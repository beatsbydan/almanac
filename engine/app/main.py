"""
Internal-only generation engine. Never reachable from the public internet — only the Core API
calls it, over the internal network (see Almanac Blueprint §09). No auth of its own: the network
boundary is the security boundary here, same as any other internal service.
"""

from fastapi import FastAPI

from app.models import GenerationRequest, GenerationResponse
from app.solver import solve

app = FastAPI(title="Almanac Generation Engine")


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/generate", response_model=GenerationResponse)
def generate(request: GenerationRequest) -> GenerationResponse:
    return solve(request)
