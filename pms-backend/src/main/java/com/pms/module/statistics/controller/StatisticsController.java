package com.pms.module.statistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pms.common.result.R;
import com.pms.module.defect.entity.PmsDefect;
import com.pms.module.defect.mapper.PmsDefectMapper;
import com.pms.module.project.entity.PmsProject;
import com.pms.module.project.mapper.PmsProjectMapper;
import com.pms.module.task.entity.PmsTask;
import com.pms.module.task.entity.PmsWorklog;
import com.pms.module.task.mapper.PmsTaskMapper;
import com.pms.module.task.mapper.PmsWorklogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class StatisticsController {

    private final PmsProjectMapper projectMapper;
    private final PmsTaskMapper taskMapper;
    private final PmsDefectMapper defectMapper;
    private final PmsWorklogMapper worklogMapper;

    @Operation(summary = "仪表盘统计")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("activeProjects", projectMapper.selectCount(
                new LambdaQueryWrapper<PmsProject>().eq(PmsProject::getStatus, "EXECUTING")));
        result.put("pendingTasks", taskMapper.selectCount(
                new LambdaQueryWrapper<PmsTask>().in(PmsTask::getStatus, "TODO", "IN_PROGRESS")));
        result.put("openDefects", defectMapper.selectCount(
                new LambdaQueryWrapper<PmsDefect>().notIn(PmsDefect::getStatus, "CLOSED", "VERIFIED")));
        // This week's hours
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        Double hours = worklogMapper.selectList(
                new LambdaQueryWrapper<PmsWorklog>().ge(PmsWorklog::getWorkDate, weekStart)
        ).stream().mapToDouble(w -> w.getHours().doubleValue()).sum();
        result.put("weekHours", String.format("%.1f", hours));
        return R.ok(result);
    }

    @Operation(summary = "项目进度EVM指标")
    @GetMapping("/evm/{projectId}")
    public R<Map<String, Object>> evm(@PathVariable Long projectId) {
        // PV: sum of planned hours for tasks planned to be completed by today
        // EV: sum of planned hours * progress% for tasks actually worked on
        // AC: sum of actual hours logged
        List<PmsTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<PmsTask>().eq(PmsTask::getProjectId, projectId));
        double pv = 0, ev = 0, ac = 0;
        LocalDate today = LocalDate.now();
        for (PmsTask t : tasks) {
            double planned = t.getPlannedHours() != null ? t.getPlannedHours().doubleValue() : 0;
            double actual = t.getActualHours() != null ? t.getActualHours().doubleValue() : 0;
            int progress = t.getProgress() != null ? t.getProgress() : 0;
            ac += actual;
            ev += planned * progress / 100.0;
            if (t.getDueDate() != null && t.getDueDate().isBefore(today)) {
                pv += planned;
            }
        }
        double spi = pv > 0 ? ev / pv : 1.0;
        double cpi = ac > 0 ? ev / ac : 1.0;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pv", String.format("%.1f", pv));
        result.put("ev", String.format("%.1f", ev));
        result.put("ac", String.format("%.1f", ac));
        result.put("spi", String.format("%.2f", spi));
        result.put("cpi", String.format("%.2f", cpi));
        result.put("status", spi >= 0.9 ? (cpi >= 0.9 ? "ON_TRACK" : "OVER_BUDGET") : "BEHIND_SCHEDULE");
        return R.ok(result);
    }

    @Operation(summary = "缺陷统计")
    @GetMapping("/defect-stats/{projectId}")
    public R<Map<String, Object>> defectStats(@PathVariable Long projectId) {
        List<PmsDefect> defects = defectMapper.selectList(
                new LambdaQueryWrapper<PmsDefect>().eq(PmsDefect::getProjectId, projectId));
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (PmsDefect d : defects) {
            bySeverity.merge(d.getSeverity(), 1L, Long::sum);
            byStatus.merge(d.getStatus(), 1L, Long::sum);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", defects.size());
        result.put("bySeverity", bySeverity);
        result.put("byStatus", byStatus);
        long closed = byStatus.getOrDefault("CLOSED", 0L) + byStatus.getOrDefault("VERIFIED", 0L);
        result.put("closeRate", defects.isEmpty() ? "0%" : String.format("%.1f%%", closed * 100.0 / defects.size()));
        return R.ok(result);
    }

    @Operation(summary = "工时统计")
    @GetMapping("/worklog-stats")
    public R<List<Map<String, Object>>> worklogStats(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LambdaQueryWrapper<PmsWorklog> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) wrapper.eq(PmsWorklog::getProjectId, projectId);
        if (userId != null) wrapper.eq(PmsWorklog::getUserId, userId);
        if (startDate != null) wrapper.ge(PmsWorklog::getWorkDate, LocalDate.parse(startDate));
        if (endDate != null) wrapper.le(PmsWorklog::getWorkDate, LocalDate.parse(endDate));
        List<PmsWorklog> logs = worklogMapper.selectList(wrapper);
        Map<String, Double> dailySum = new LinkedHashMap<>();
        for (PmsWorklog l : logs) {
            dailySum.merge(l.getWorkDate().toString(), l.getHours().doubleValue(), Double::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        dailySum.forEach((k, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", k);
            m.put("hours", String.format("%.1f", v));
            result.add(m);
        });
        return R.ok(result);
    }
}
