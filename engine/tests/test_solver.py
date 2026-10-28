from app.models import BlackoutWindow, GenerationRequest, GenerationStatus, Room, Session
from app.solver import solve

DAYS = ["Mondays", "Tuesdays", "Wednesdays"]
HOURS = [8, 9, 10, 11]
MAX_END_HOUR = 12


def _request(sessions, rooms=None, blackout_windows=None):
    return GenerationRequest(
        tenant_id="t1",
        days=DAYS,
        period_start_hours=HOURS,
        max_end_hour=MAX_END_HOUR,
        blackout_windows=blackout_windows or [],
        rooms=rooms or [Room(id="r1", capacity=50), Room(id="r2", capacity=50)],
        sessions=sessions,
    )


def test_single_session_is_solved():
    request = _request(
        [
            Session(
                id="s1",
                teacher_id="teacher-1",
                duration_hours=1,
                expected_size=30,
                eligible_room_ids=["r1", "r2"],
                participant_group_ids=["g1"],
            )
        ]
    )

    result = solve(request)

    assert result.status == GenerationStatus.SOLVED
    assert len(result.assignments) == 1
    assignment = result.assignments[0]
    assert assignment.session_id == "s1"
    assert assignment.day in DAYS
    assert assignment.start_hour in HOURS
    assert assignment.room_id in ("r1", "r2")


def test_sessions_sharing_a_participant_group_never_overlap():
    """Two sessions for the same department+level can't both be given the same day/hour, even
    though there are two rooms free and two different teachers — the conflict is the participant
    group, not the room or the teacher."""
    sessions = [
        Session(
            id="s1",
            teacher_id="teacher-1",
            duration_hours=1,
            expected_size=10,
            eligible_room_ids=["r1", "r2"],
            participant_group_ids=["dept-A"],
        ),
        Session(
            id="s2",
            teacher_id="teacher-2",
            duration_hours=1,
            expected_size=10,
            eligible_room_ids=["r1", "r2"],
            participant_group_ids=["dept-A"],
        ),
    ]

    result = solve(_request(sessions))

    assert result.status == GenerationStatus.SOLVED
    by_id = {a.session_id: a for a in result.assignments}
    s1, s2 = by_id["s1"], by_id["s2"]
    assert not (s1.day == s2.day and s1.start_hour == s2.start_hour)


def test_two_sessions_needing_the_same_single_room_do_not_double_book_it():
    sessions = [
        Session(
            id="s1", teacher_id="teacher-1", duration_hours=1, expected_size=10, eligible_room_ids=["r1"], participant_group_ids=["g1"]
        ),
        Session(
            id="s2", teacher_id="teacher-2", duration_hours=1, expected_size=10, eligible_room_ids=["r1"], participant_group_ids=["g2"]
        ),
    ]

    result = solve(_request(sessions, rooms=[Room(id="r1", capacity=50)]))

    assert result.status == GenerationStatus.SOLVED
    by_id = {a.session_id: a for a in result.assignments}
    s1, s2 = by_id["s1"], by_id["s2"]
    assert not (s1.day == s2.day and s1.start_hour == s2.start_hour)


def test_same_teacher_on_two_sessions_never_overlaps_even_with_different_rooms_and_groups():
    """One lecturer teaching two different courses to two different departments can't be in two
    rooms at once — the conflict here is purely the teacher, nothing else."""
    sessions = [
        Session(
            id="s1",
            teacher_id="shared-teacher",
            duration_hours=1,
            expected_size=10,
            eligible_room_ids=["r1"],
            participant_group_ids=["dept-A"],
        ),
        Session(
            id="s2",
            teacher_id="shared-teacher",
            duration_hours=1,
            expected_size=10,
            eligible_room_ids=["r2"],
            participant_group_ids=["dept-B"],
        ),
    ]

    result = solve(_request(sessions))

    assert result.status == GenerationStatus.SOLVED
    by_id = {a.session_id: a for a in result.assignments}
    s1, s2 = by_id["s1"], by_id["s2"]
    assert not (s1.day == s2.day and s1.start_hour == s2.start_hour)


def test_session_too_big_for_every_room_is_reported_infeasible():
    sessions = [
        Session(
            id="s1", teacher_id="teacher-1", duration_hours=1, expected_size=10, eligible_room_ids=["r1"], participant_group_ids=["g1"]
        ),
        Session(
            id="oversized",
            teacher_id="teacher-2",
            duration_hours=1,
            expected_size=999,
            eligible_room_ids=["r1"],
            participant_group_ids=["g1"],
        ),
    ]

    result = solve(_request(sessions, rooms=[Room(id="r1", capacity=50)]))

    assert result.status == GenerationStatus.INFEASIBLE
    assert result.infeasible_session_ids == ["oversized"]


def test_blackout_window_removes_the_slots_it_covers():
    """Blackout covers every hour except 11am; the solver must land there, not inside the window."""
    blackout = [BlackoutWindow(day=day, start_hour=8, end_hour=11) for day in DAYS]
    sessions = [
        Session(
            id="s1", teacher_id="teacher-1", duration_hours=1, expected_size=10, eligible_room_ids=["r1"], participant_group_ids=["g1"]
        )
    ]

    result = solve(_request(sessions, rooms=[Room(id="r1", capacity=50)], blackout_windows=blackout))

    assert result.status == GenerationStatus.SOLVED
    assert result.assignments[0].start_hour == 11


def test_duration_that_cannot_fit_before_max_end_hour_is_infeasible():
    sessions = [
        Session(
            id="too_long",
            teacher_id="teacher-1",
            duration_hours=5,
            expected_size=10,
            eligible_room_ids=["r1"],
            participant_group_ids=["g1"],
        )
    ]

    result = solve(_request(sessions, rooms=[Room(id="r1", capacity=50)]))

    assert result.status == GenerationStatus.INFEASIBLE
    assert result.infeasible_session_ids == ["too_long"]
