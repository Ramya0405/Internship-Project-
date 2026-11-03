package com.farmchainx.controller;

import com.farmchainx.dto.SupplyChainEventDto;
import com.farmchainx.enums.EventType;
import com.farmchainx.security.CustomUserDetails;
import com.farmchainx.service.NotificationService;
import com.farmchainx.service.ProductBatchService;
import com.farmchainx.service.SupplyChainEventService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
public class SupplyChainEventController extends DashboardController {

    private final SupplyChainEventService eventService;
    private final ProductBatchService     batchService;

    public SupplyChainEventController(SupplyChainEventService eventService,
                                      ProductBatchService batchService,
                                      NotificationService notificationService) {
        super(notificationService);
        this.eventService = eventService;
        this.batchService = batchService;
    }

    @GetMapping("/batch/{batchId}")
    public String history(@PathVariable Long batchId,
                          @AuthenticationPrincipal CustomUserDetails principal,
                          Model model) {
        addCommonAttributes(model, principal);
        model.addAttribute("events",     eventService.getHistoryForBatch(batchId, principal.getUser()));
        model.addAttribute("batch",      batchService.findById(batchId, principal.getUser()));
        SupplyChainEventDto eventDto = new SupplyChainEventDto();
        eventDto.setBatchId(batchId);
        model.addAttribute("eventDto",   eventDto);
        model.addAttribute("eventTypes", EventType.values());
        return "events/history";
    }

    @PostMapping("/batch/{batchId}/add")
    public String addEvent(@PathVariable Long batchId,
                           @Valid @ModelAttribute("eventDto") SupplyChainEventDto dto,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal CustomUserDetails principal,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            addCommonAttributes(model, principal);
            model.addAttribute("events",     eventService.getHistoryForBatch(batchId, principal.getUser()));
            model.addAttribute("batch",      batchService.findById(batchId, principal.getUser()));
            model.addAttribute("eventTypes", EventType.values());
            return "events/history";
        }
        dto.setBatchId(batchId);
        try {
            eventService.addEvent(dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMsg", "Supply chain event recorded.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/events/batch/" + batchId;
    }
}
