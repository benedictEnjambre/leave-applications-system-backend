package com.synacy.leavesmanagement.leaveapplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeaveApplicationController {
    private final LeaveApplicationService leaveApplicationService;

    @Autowired
    public LeaveApplicationController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/{userId}/leave-application")
    public LeaveApplicationResponse createLeaveApplication(@PathVariable Long userId, @RequestBody LeaveApplicationRequest request){
        LeaveApplication leaveApplication = leaveApplicationService.applyLeave(userId, request);
        return new LeaveApplicationResponse(leaveApplication);
    }


//
//    @PutMapping("api/v1/leave-applications/{leaveAppId}")
//    public LeaveApplicationResponse updateLeaveApplication(@PathVariable Long leaveAppId,
//                                                   @RequestParam Long approverId,
//                                                   @RequestParam LeaveStatus status,
//                                                   @RequestParam(required = false) String remarks) {
//        return leaveApplicationService.updateLeaveStatus(approverId, leaveAppId, status, remarks);
//    }
//
//    @GetMapping("api/v1/leave-application/{id}")
//    public LeaveApplication getLeaveApplication(@PathVariable Long id,
//                                                @RequestParam(value = "page", defaultValue = "1") int page,
//                                                @RequestParam(value = "max",defaultValue = "5") int max){
//        Page<LeaveApplication> applications =
//
//    }



}
