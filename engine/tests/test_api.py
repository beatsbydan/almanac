from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_generate_endpoint_solves_a_simple_request():
    payload = {
        "tenant_id": "t1",
        "days": ["Mondays"],
        "period_start_hours": [8, 9],
        "max_end_hour": 10,
        "rooms": [{"id": "r1", "capacity": 50}],
        "sessions": [
            {
                "id": "s1",
                "teacher_id": "teacher-1",
                "duration_hours": 1,
                "expected_size": 20,
                "eligible_room_ids": ["r1"],
                "participant_group_ids": ["g1"],
            }
        ],
    }

    response = client.post("/generate", json=payload)

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "SOLVED"
    assert len(body["assignments"]) == 1
    assert body["assignments"][0]["session_id"] == "s1"
