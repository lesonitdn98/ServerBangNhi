package com.bangnhi.note.controller;

import com.bangnhi.note.data.model.Note;
import com.bangnhi.note.data.model.User;
import com.bangnhi.note.data.repository.JWTRepository;
import com.bangnhi.note.data.repository.NoteRepository;
import com.bangnhi.note.data.repository.UserRepository;
import com.bangnhi.note.data.response.BaseResponse;
import com.bangnhi.note.utils.AppUtils;
import com.bangnhi.note.utils.JwtTokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("notes")
public class NoteController {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final JWTRepository jwtRepository;

    public NoteController(
            NoteRepository noteRepository,
            UserRepository userRepository,
            JWTRepository jwtRepository
    ) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.jwtRepository = jwtRepository;
    }

    @GetMapping
    public @ResponseBody
    ResponseEntity<BaseResponse<Iterable<Note>>> getNotes(
            @RequestHeader("Authorization") String auth
    ) {
        BaseResponse<Iterable<Note>> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(true, "", noteRepository.findAll());
            status = HttpStatus.OK;
        } else {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(responseBody, status);
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<Object>> addNote(
            @RequestHeader("Authorization") String auth,
            @RequestParam String title,
            @RequestParam String description
    ) {
        BaseResponse<Object> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else if (title.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Title is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else {
            Long id = JwtTokenUtils.getUserIdFromJWT(token);
            Note newNote = new Note(
                    userRepository.findById(id),
                    title,
                    description,
                    new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date())
            );
            noteRepository.save(newNote);

            responseBody = new BaseResponse<>(true, "Add Note Success");
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(responseBody, status);
    }

    @GetMapping("/note/{noteId}")
    public @ResponseBody
    ResponseEntity<BaseResponse<Note>> getNote(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long noteId
    ) {
        BaseResponse<Note> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else {
            Note note = noteRepository.findNoteById(noteId);
            if (note == null) {
                responseBody = new BaseResponse<>(true, "");
                status = HttpStatus.NOT_FOUND;
            } else {
                responseBody = new BaseResponse<>(true, "", note);
                status = HttpStatus.OK;
            }
        }
        return new ResponseEntity<>(responseBody, status);
    }

    @PostMapping("/edit/{noteId}")
    public ResponseEntity<BaseResponse<Note>> editNote(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long noteId,
            @RequestParam String title,
            @RequestParam String description
    ) {
        BaseResponse<Note> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else if (title.isEmpty()) {
            responseBody = new BaseResponse<>(false, "Title is Empty!");
            status = HttpStatus.BAD_REQUEST;
        } else {
            Note note = noteRepository.findNoteById(noteId);
            Long userId = JwtTokenUtils.getUserIdFromJWT(token);
            if (!note.getUser().getId().equals(userRepository.findById(userId).getId())) {
                responseBody = new BaseResponse<>(false, "Edit Failed!");
                status = HttpStatus.FORBIDDEN;
            } else {
                note.setTitle(title);
                note.setDescription(description);
                noteRepository.save(note);
                responseBody = new BaseResponse<>(true, "Edit Success!", noteRepository.findNoteById(noteId));
                status = HttpStatus.OK;
            }
        }
        return new ResponseEntity<>(responseBody, status);
    }

    @PostMapping("/remove/{noteId}")
    @Transactional
    public ResponseEntity<BaseResponse<Object>> removeNote(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long noteId
    ) {
        BaseResponse<Object> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else {
            Note note = noteRepository.findNoteById(noteId);
            Long userId = JwtTokenUtils.getUserIdFromJWT(token);
            if (!note.getUser().getId().equals(userRepository.findById(userId).getId())) {
                responseBody = new BaseResponse<>(false, "Remove Failed!");
                status = HttpStatus.FORBIDDEN;
            } else {
                noteRepository.deleteNoteById(noteId);
                responseBody = new BaseResponse<>(true, "Remove Success!", noteRepository.findNoteById(noteId));
                status = HttpStatus.OK;
            }
        }
        return new ResponseEntity<>(responseBody, status);
    }

    @GetMapping("/search")
    public @ResponseBody
    ResponseEntity<BaseResponse<List<Note>>> searchNotes(
            @RequestHeader("Authorization") String auth,
            @RequestParam String keyword
    ) {
        BaseResponse<List<Note>> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else if (keyword.trim().isEmpty()) {
            List<Note> notes = (List<Note>) noteRepository.findAll();
            responseBody = new BaseResponse<>(true, notes.size() + " results", notes);
            status = HttpStatus.OK;
        } else {
            List<Note> notes = noteRepository.search(keyword.toLowerCase().trim());
            responseBody = new BaseResponse<>(true, notes.size() + " results", notes);
            status = HttpStatus.OK;

        }
        return new ResponseEntity<>(responseBody, status);
    }

    @GetMapping("/users/{userId}")
    public @ResponseBody
    ResponseEntity<BaseResponse<List<Note>>> getNoteByUser(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long userId
    ) {
        BaseResponse<List<Note>> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else {
            User user = userRepository.findById(userId);
            if (user == null) {
                responseBody = new BaseResponse<>(false, "User does not exist!");
                status = HttpStatus.NOT_FOUND;
            } else {
                List<Note> notes = noteRepository.findAllByUser(user);
                responseBody = new BaseResponse<>(true, notes.size() + " results", notes);
                status = HttpStatus.OK;
            }
        }
        return new ResponseEntity<>(responseBody, status);
    }

    @GetMapping("/users/{userId}/search")
    public @ResponseBody
    ResponseEntity<BaseResponse<List<Note>>> searchNotes(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long userId,
            @RequestParam String keyword
    ) {
        BaseResponse<List<Note>> responseBody;
        HttpStatus status;
        String token = JwtTokenUtils.getJwtFromRequest(auth);
        if (!AppUtils.validateAuthToken(token, jwtRepository)) {
            responseBody = new BaseResponse<>(false, "Unauthorized");
            status = HttpStatus.UNAUTHORIZED;
        } else {
            User user = userRepository.findById(userId);
            if (user == null) {
                responseBody = new BaseResponse<>(false, "User does not exist!");
                status = HttpStatus.NOT_FOUND;
            } else {
                List<Note> notes = noteRepository.searchInMyNotes(user, keyword.toLowerCase().trim());
                responseBody = new BaseResponse<>(true, notes.size() + " results", notes);
                status = HttpStatus.OK;
            }
        }
        return new ResponseEntity<>(responseBody, status);
    }
}
