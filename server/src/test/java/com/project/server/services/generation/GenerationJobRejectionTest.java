package com.project.server.services.generation;

import com.project.server.BaseIntegrationTest;
import com.project.server.db.models.GenerationJob;
import com.project.server.db.services.generationJob.GenerationJobService;
import com.project.server.dtos.request.generation.GenerationTriggerRequest;
import com.project.server.dtos.response.generation.GenerationJobResponse;
import com.project.server.enums.GenerationJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

/**
 * Found via k6 load testing (115 concurrent /api/v1/generation triggers against AsyncConfig's
 * corePoolSize=2/maxPoolSize=10/queueCapacity=100 executor): once capacity is exceeded,
 * ThreadPoolExecutor's AbortPolicy throws TaskRejectedException synchronously back onto the
 * request thread, inside trigger() — before GenerationOrchestrationService.trigger()'s fix, that
 * propagated as a bare 500 AND left the GenerationJob row (created before the dispatch attempt)
 * stuck in PENDING forever, since nothing downstream ever touched it again. Mocking
 * GenerationWorker to throw deterministically reproduces the executor-saturation case without
 * actually needing 110+ real concurrent requests.
 */
@org.springframework.boot.test.context.SpringBootTest
class GenerationJobRejectionTest extends BaseIntegrationTest {
    @Autowired
    private GenerationOrchestrationService generationOrchestrationService;
    @Autowired
    private GenerationJobService generationJobService;
    @MockitoBean
    private GenerationWorker generationWorker;

    @BeforeEach
    void setUp() {
        super.setup();
    }

    @Test
    void trigger_marksTheJobFailedInsteadOfLeavingItPendingForever_whenTheExecutorRejectsTheDispatch() throws Exception {
        doThrow(new TaskRejectedException("Executor queue is full"))
                .when(generationWorker).run(any(), anyLong(), any());

        GenerationJobResponse response = generationOrchestrationService.trigger(
                GenerationTriggerRequest.builder().lecturerAssignments(Map.of()).build());

        assertEquals(GenerationJobStatus.FAILED, response.getStatus());
        assertNotNull(response.getMessage());

        GenerationJob persisted = generationJobService.getByCode(response.getCode());
        assertEquals(GenerationJobStatus.FAILED, persisted.getStatus());
        assertNotEquals(GenerationJobStatus.PENDING, persisted.getStatus());
    }
}
