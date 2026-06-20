package com.github.diogocerqueiralima.application.results;

import java.util.List;

/**
 *
 * Represents the result of a vehicle assignment history query.
 *
 * @param assignments the list of vehicle assignment results representing the history of assignments for a device or vehicle
 */
public record VehicleAssignmentHistoryResult(
        List<VehicleAssignmentResult> assignments,
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements
) {}