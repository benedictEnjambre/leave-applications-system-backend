package com.synacy.leavesmanagement.leaveapplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeaveApplicationController {
    private final LeaveApplicationService leaveApplicationService;

    @Autowired
    public LeaveApplicationController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @PostMapping("/api/v1/{userId}/leave-application")
    public LeaveApplication createLeaveApplication(@PathVariable Long userId, @RequestBody LeaveApplicationRequest request){
        return leaveApplicationService.applyLeave(userId, request);
    }

    @PutMapping("api/v1/leave-applications/{leaveAppId}")
    public LeaveApplicationResponse updateLeaveApplication(@PathVariable Long leaveAppId,
                                                   @RequestParam Long approverId,
                                                   @RequestParam LeaveStatus status,
                                                   @RequestParam(required = false) String remarks) {
        return leaveApplicationService.updateLeaveStatus(approverId, leaveAppId, status, remarks);
    }





}
