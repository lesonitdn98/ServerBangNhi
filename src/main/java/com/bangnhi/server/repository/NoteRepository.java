package com.bangnhi.server.repository;

import com.bangnhi.server.model.Note;
import com.bangnhi.server.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoteRepository extends CrudRepository<Note, Integer> {
    Note findNoteById(Long id);
    List<Note> findAllByUser(User user);
    @Query("SELECT n FROM Note n WHERE CONCAT(lower(n.title), '') LIKE %?1%")
    List<Note> search(String keyword);
    @Query("SELECT n FROM Note n WHERE n.user = ?1 AND CONCAT(lower(n.title), '') LIKE %?2%")
    List<Note> searchInMyNotes(User user, String keyword);
    void deleteNoteById(Long id);
}
