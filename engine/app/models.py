from enum import Enum

from pydantic import BaseModel, Field


class Room(BaseModel):
    id: str
    capacity: int


class BlackoutWindow(BaseModel):
    day: str
    start_hour: int
    end_hour: int


class Session(BaseModel):
    """One thing that needs a day/hour/room assigned to it — a university course section or a
    K-12 subject-to-class slot, the server already flattened it to this shape."""

    id: str
    teacher_id: str
    duration_hours: int = Field(gt=0)
    expected_size: int = Field(ge=0)
    eligible_room_ids: list[str]
    participant_group_ids: list[str] = Field(
        description="Groups (e.g. department+level) that cannot have two sessions at once"
    )


class GenerationRequest(BaseModel):
    tenant_id: str
    days: list[str]
    period_start_hours: list[int]
    max_end_hour: int
    blackout_windows: list[BlackoutWindow] = []
    rooms: list[Room]
    sessions: list[Session]
    time_limit_seconds: float = 10.0


class GenerationStatus(str, Enum):
    SOLVED = "SOLVED"
    INFEASIBLE = "INFEASIBLE"
    NO_SOLUTION_FOUND = "NO_SOLUTION_FOUND"


class Assignment(BaseModel):
    session_id: str
    day: str
    start_hour: int
    room_id: str


class GenerationResponse(BaseModel):
    status: GenerationStatus
    assignments: list[Assignment] = []
    infeasible_session_ids: list[str] = Field(
        default=[],
        description="Sessions with zero structurally valid (day, hour, room) option — "
        "no room is both big enough and free of a blackout window for the full duration.",
    )
