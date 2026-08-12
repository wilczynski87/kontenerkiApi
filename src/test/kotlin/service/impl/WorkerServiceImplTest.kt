package com.kontenery.service.impl

import com.kontenery.data.gate.GateEventDto
import com.kontenery.data.worker.Worker
import com.kontenery.repository.GateEventRepo
import com.kontenery.repository.WorkerRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WorkerServiceImplTest {

    private lateinit var workerRepo: WorkerRepo
    private lateinit var gateEventRepo: GateEventRepo
    private lateinit var service: WorkerServiceImpl

    @BeforeEach
    fun setUp() {
        workerRepo = mockk()
        gateEventRepo = mockk()
        service = WorkerServiceImpl(workerRepo = workerRepo, gateEventRepo = gateEventRepo)
    }

    @Test
    fun `listGateEventsForWorker returns events when worker belongs to client`() = runTest {
        coEvery { workerRepo.findWorkerById(5L) } returns Worker(
            id = 5L,
            clientId = 1L,
            name = "Jan",
            email = "jan@example.com",
        )
        val events = listOf(GateEventDto(openedAtEpochMs = 1_700_000_000_000L))
        coEvery { gateEventRepo.listOpenEventsByWorkerId(5L, 50) } returns events

        val result = service.listGateEventsForWorker(clientId = 1L, workerId = 5L, limit = 50)

        assertEquals(events, result)
        coVerify { gateEventRepo.listOpenEventsByWorkerId(5L, 50) }
    }

    @Test
    fun `listGateEventsForWorker throws when worker not found`() = runTest {
        coEvery { workerRepo.findWorkerById(5L) } returns null

        assertThrows<IllegalArgumentException> {
            service.listGateEventsForWorker(clientId = 1L, workerId = 5L)
        }
    }

    @Test
    fun `listGateEventsForWorker throws when worker belongs to another client`() = runTest {
        coEvery { workerRepo.findWorkerById(5L) } returns Worker(
            id = 5L,
            clientId = 2L,
            name = "Jan",
            email = "jan@example.com",
        )

        assertThrows<IllegalArgumentException> {
            service.listGateEventsForWorker(clientId = 1L, workerId = 5L)
        }
    }
}
