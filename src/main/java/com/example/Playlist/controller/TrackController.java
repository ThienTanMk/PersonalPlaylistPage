package com.example.Playlist.controller;

import com.example.Playlist.dto.TrackDto;
import com.example.Playlist.service.TrackService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TrackController {
    TrackService trackService;

    @GetMapping
    public String getAllTracks(Model model) {
        List<TrackDto> tracks = trackService.getAllTracks();
        model.addAttribute("tracks", tracks);
        return "tracks";
    }

    @PostMapping("/update")
    public String updateTrack(@ModelAttribute TrackDto trackDto, RedirectAttributes redirectAttributes) {
        trackService.updateTrack(trackDto);
        redirectAttributes.addAttribute("message", "Cập nhật thành công!");
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteTrack(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        trackService.deleteTrack(id);
        redirectAttributes.addAttribute("message", "Xóa thành công!");
        return "redirect:/";
    }

}
