package com.synacy.leavesmanagement.web;

import java.util.List;

public record PageResponse<T>(int totalCount, int pageNumber, List<T> content) {
}
