package com.bangnhi.server.controller;

import com.bangnhi.server.model.Note;
import com.bangnhi.server.model.User;
import com.bangnhi.server.repository.NoteRepository;
import com.bangnhi.server.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteController(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public @ResponseBody
    Iterable<Note> getNotes() {
        return noteRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addNote(@RequestParam String title, @RequestParam String description) {
        if (title.isEmpty()) {
            return new ResponseEntity<>("Title is Empty!", null, HttpStatus.BAD_REQUEST);
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Note newNote = new Note(userRepository.findByUsername(username), title, description, new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
        noteRepository.save(newNote);
        return new ResponseEntity<>("Add Note Success", null, HttpStatus.OK);
    }

    @GetMapping("/{noteId}")
    public @ResponseBody
    Note getNote(@PathVariable Long noteId) {
        return noteRepository.findNoteById(noteId);
    }

    @PostMapping("/edit/{noteId}")
    public ResponseEntity<String> editNote(@PathVariable Long noteId, @RequestParam String title, @RequestParam String description) {
        if (title.isEmpty()) {
            return new ResponseEntity<>("Title is Empty!", null, HttpStatus.BAD_REQUEST);
        }
        Note note = noteRepository.findNoteById(noteId);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!note.getUser().getId().equals(userRepository.findByUsername(username).getId())) {
            return new ResponseEntity<>("Edit Failed!", null, HttpStatus.BAD_REQUEST);
        }
        note.setTitle(title);
        note.setDescription(description);
        noteRepository.save(note);
        return new ResponseEntity<>("Edit Note Success", null, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public @ResponseBody
    List<Note> getNoteByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId);
        return noteRepository.findAllByUser(user);
    }

    @GetMapping("/user/{userId}/search")
    public @ResponseBody
    List<Note> searchNotes(@PathVariable Long userId, @RequestParam String keyword) {
        User user = userRepository.findById(userId);
        if (keyword.trim().isEmpty()) {
            return noteRepository.findAllByUser(user);
        }
        return noteRepository.searchInMyNotes(user, keyword.toLowerCase().trim());
    }

    @GetMapping("/search")
    public @ResponseBody
    List<Note> searchNotes(@RequestParam String keyword) {
        if (keyword.trim().isEmpty()) {
            return (List<Note>) noteRepository.findAll();
        }
        return noteRepository.search(keyword.toLowerCase().trim());
    }

    @PostMapping("/remove/{noteId}")
    @Transactional
    public ResponseEntity<String> removeNote(@PathVariable Long noteId) {
        noteRepository.deleteNoteById(noteId);
        return new ResponseEntity<>("Delete Note Success", null, HttpStatus.OK);
    }
}
