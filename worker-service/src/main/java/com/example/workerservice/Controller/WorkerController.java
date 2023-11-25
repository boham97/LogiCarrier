package com.example.workerservice.Controller;

import com.example.workerservice.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/worker")
public class WorkerController {
    private final RedisService redisService;

    @GetMapping("/workerBit")
    public String getWorkerBit() {
        return redisService.getBit("workerBit");
    }

    @GetMapping("/progressBit")
    public String getProgressBit() {
        return redisService.getBit("progressBit");
    }

    @PostMapping("")
    public void setInitialBits() {
        redisService.setInitialWorkerBit();
        redisService.setInitialProgressBit();
    }

    @PostMapping("/workerBit/{workerId}")
    public String setWorkerBit(@PathVariable("workerId") Long id) {
        return redisService.setWorkerBit(id);
    }

    @PostMapping("/progressBit/{workerId}")
    public String setProgressBit(@PathVariable("workerId") Long id) {
        return redisService.setProgressBit(id);
    }
}