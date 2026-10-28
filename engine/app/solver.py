"""
The actual constraint-based generator — what Almanac's Blueprint calls for in place of the old
Java service's manual schedule entry and random-sampling "suggestions". Given a tenant's rooms,
calendar (days/hours/blackout windows), and the sessions that need placing, it either returns one
assignment per session that satisfies every hard constraint, or reports which sessions have no
valid placement at all.

Hard constraints enforced:
  - a session's duration must fit inside the tenant's working hours and not cross a blackout window
  - a room must have enough capacity for the session's expected size
  - no room holds two sessions at an overlapping hour
  - no teacher holds two sessions at an overlapping hour
  - no participant group (e.g. a department+level) has two sessions at an overlapping hour

This is a feasibility model, not an optimizing one — no soft-constraint objective yet (minimizing
gaps, balancing teacher load). That's deliberately deferred: get hard-constraint correctness solid
first.
"""

from ortools.sat.python import cp_model

from app.models import Assignment, BlackoutWindow, GenerationRequest, GenerationResponse, GenerationStatus, Room, Session


def _overlaps_blackout(day: str, start_hour: int, end_hour: int, blackout_windows: list[BlackoutWindow]) -> bool:
    for window in blackout_windows:
        if window.day == day and start_hour < window.end_hour and end_hour > window.start_hour:
            return True
    return False


def _session_options(
    session: Session,
    days: list[str],
    period_start_hours: list[int],
    max_end_hour: int,
    blackout_windows: list[BlackoutWindow],
    rooms_by_id: dict[str, Room],
) -> list[tuple[str, int, str]]:
    """Every structurally valid (day, start_hour, room_id) triple for this session — before any
    cross-session conflict is considered."""
    eligible_rooms = [rooms_by_id[rid] for rid in session.eligible_room_ids if rid in rooms_by_id]
    big_enough_rooms = [room for room in eligible_rooms if room.capacity >= session.expected_size]

    options: list[tuple[str, int, str]] = []
    for day in days:
        for start_hour in period_start_hours:
            end_hour = start_hour + session.duration_hours
            if end_hour > max_end_hour:
                continue
            if _overlaps_blackout(day, start_hour, end_hour, blackout_windows):
                continue
            for room in big_enough_rooms:
                options.append((day, start_hour, room.id))
    return options


def solve(request: GenerationRequest) -> GenerationResponse:
    rooms_by_id = {room.id: room for room in request.rooms}

    session_options: dict[str, list[tuple[str, int, str]]] = {
        session.id: _session_options(
            session,
            request.days,
            request.period_start_hours,
            request.max_end_hour,
            request.blackout_windows,
            rooms_by_id,
        )
        for session in request.sessions
    }

    infeasible_session_ids = [sid for sid, opts in session_options.items() if not opts]
    if infeasible_session_ids:
        return GenerationResponse(status=GenerationStatus.INFEASIBLE, infeasible_session_ids=infeasible_session_ids)

    model = cp_model.CpModel()
    choice_vars: dict[tuple[str, int], cp_model.IntVar] = {}

    for session in request.sessions:
        options = session_options[session.id]
        option_vars = []
        for i in range(len(options)):
            var = model.new_bool_var(f"choice_{session.id}_{i}")
            choice_vars[(session.id, i)] = var
            option_vars.append(var)
        model.add_exactly_one(option_vars)

    room_hour_occupants: dict[tuple[str, int, str], list[cp_model.IntVar]] = {}
    teacher_hour_occupants: dict[tuple[str, int, str], list[cp_model.IntVar]] = {}
    group_hour_occupants: dict[tuple[str, int, str], list[cp_model.IntVar]] = {}

    for session in request.sessions:
        options = session_options[session.id]
        for i, (day, start_hour, room_id) in enumerate(options):
            var = choice_vars[(session.id, i)]
            for hour in range(start_hour, start_hour + session.duration_hours):
                room_hour_occupants.setdefault((day, hour, room_id), []).append(var)
                teacher_hour_occupants.setdefault((day, hour, session.teacher_id), []).append(var)
                for group_id in session.participant_group_ids:
                    group_hour_occupants.setdefault((day, hour, group_id), []).append(var)

    for occupants in room_hour_occupants.values():
        if len(occupants) > 1:
            model.add_at_most_one(occupants)

    for occupants in teacher_hour_occupants.values():
        if len(occupants) > 1:
            model.add_at_most_one(occupants)

    for occupants in group_hour_occupants.values():
        if len(occupants) > 1:
            model.add_at_most_one(occupants)

    solver = cp_model.CpSolver()
    solver.parameters.max_time_in_seconds = request.time_limit_seconds
    status = solver.solve(model)

    if status in (cp_model.OPTIMAL, cp_model.FEASIBLE):
        assignments = []
        for session in request.sessions:
            options = session_options[session.id]
            for i, (day, start_hour, room_id) in enumerate(options):
                if solver.value(choice_vars[(session.id, i)]):
                    assignments.append(Assignment(session_id=session.id, day=day, start_hour=start_hour, room_id=room_id))
                    break
        return GenerationResponse(status=GenerationStatus.SOLVED, assignments=assignments)

    if status == cp_model.INFEASIBLE:
        return GenerationResponse(status=GenerationStatus.INFEASIBLE)

    return GenerationResponse(status=GenerationStatus.NO_SOLUTION_FOUND)
