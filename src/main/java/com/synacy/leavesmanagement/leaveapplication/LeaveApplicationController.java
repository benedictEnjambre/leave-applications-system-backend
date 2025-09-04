package com.synacy.leavesmanagement.leaveapplication;

import com.synacy.leavesmanagement.web.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LeaveApplicationController {
    private final LeaveApplicationService leaveApplicationService;

    @Autowired
    public LeaveApplicationController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v1/leave-application/{userId}")
    public LeaveApplicationResponse createLeaveApplication(@PathVariable Long userId, @RequestBody LeaveApplicationRequest request) {
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
    @GetMapping("/api/v1/leave-application/{userId}/me")
    public PageResponse<LeaveApplicationResponse> fetchMyLeaves(@PathVariable Long userId,
                                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                                @RequestParam(value = "max", defaultValue = "5") int max) {
        return buildPageResponse(leaveApplicationService.fetchOwnLeaveApplication(userId, page, max), page);
    }

    @GetMapping("/api/v1/leave-application/{userId}/team")
    public PageResponse<LeaveApplicationResponse> fetchTeamLeave(@PathVariable Long userId,
                                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                                @RequestParam(value = "max", defaultValue = "5") int max) {
        return buildPageResponse(leaveApplicationService.fetchTeamLeaveApplication(userId, page, max), page);
    }

    @GetMapping("/api/v1/leave-application/{userId}/all")
    public PageResponse<LeaveApplicationResponse> fetchAllLeave(@PathVariable Long userId,
                                                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                                                 @RequestParam(value = "max", defaultValue = "5") int max) {
        return buildPageResponse(leaveApplicationService.fetchAllLeaveApplication(userId, page, max), page);
    }

    private PageResponse<LeaveApplicationResponse> buildPageResponse(Page<LeaveApplication> applications, int page) {
        List<LeaveApplicationResponse> content = applications.getContent()
                .stream()
                .map(LeaveApplicationResponse::new)
                .toList();

        return new PageResponse<>((int) applications.getTotalElements(), page, content);
    }

    @PatchMapping("api/v1/leave-application/{userId}/{leaveId}/cancel")
    public LeaveApplicationResponse cancelLeave(@PathVariable Long userId,
                                                @PathVariable Long leaveId) {
        LeaveApplication application = leaveApplicationService.cancelLeave(userId, leaveId);
        return new LeaveApplicationResponse(application);
    }

    @PatchMapping("api/v1/leave-application/{userId}/{leaveId}/approve")
    public LeaveApplicationResponse approveLeave(@PathVariable Long userId,
                                                 @PathVariable Long leaveId) {
        LeaveApplication application = leaveApplicationService.approveLeave(userId, leaveId);
        return new LeaveApplicationResponse(application);
    }

    @PatchMapping("api/v1/leave-application/{userId}/{leaveId}/reject")
    public LeaveApplicationResponse rejectLeave(@PathVariable Long userId,
                                                 @PathVariable Long leaveId) {
        LeaveApplication application = leaveApplicationService.rejectLeave(userId, leaveId);
        return new LeaveApplicationResponse(application);
    }




}
