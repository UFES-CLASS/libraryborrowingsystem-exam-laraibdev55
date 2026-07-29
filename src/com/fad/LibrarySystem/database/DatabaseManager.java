/**
 * @author      masjohncook
 * @version     0.0.1
 * @copyright   (C) Copyright 2026
 * @license     None
 * @maintainer  masjohncook
 * @email       mas.john.cook@gmail.com
 * @status      Development
 */
package com.fad.LibrarySystem.database;

import com.fad.LibrarySystem.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data access layer — handles all communication with the SQLite database.
 *
 * DatabaseManager is a static utility class. It maintains a single shared
 * connection to library.db and exposes methods for every SQL operation
 * the application needs (schema creation, inserts, updates, deletes, selects).
 *
 * Design notes:
 *   - All methods are static; no instance is ever created.
 *   - A single Connection is reused across the application lifetime and
 *     closed via the shutdown hook registered in Main/App.
 *   - All queries use PreparedStatement to prevent SQL injection.
 *   - isTableEmpty() validates the table name against a whitelist before
 *     interpolating it into a query (the one place a table name must be
 *     built dynamically).
 *   - Every method throws a RuntimeException on SQL failure so callers
 *     do not need checked-exception boilerplate for errors that are not
 *     recoverable at runtime.
 *
 * Tables managed:
 *   - books                : Book catalog
 *   - multimedia           : Multimedia catalog
 *   - members              : Registered members
 *   - member_borrowed_items: Join table tracking what each member currently has on loan
 *   - borrow_records       : Full history of every borrow transaction
 *   - reservations         : Active and cancelled holds placed by members
 *   - fines                : Late-return penalties issued to members
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static Connection conn;

    /** Whitelist used by isTableEmpty() to prevent SQL injection via table name. */
    private static final Set<String> KNOWN_TABLES = Set.of(
            "books", "multimedia", "members", "member_borrowed_items",
            "borrow_records", "reservations", "fines");

    // ── Connection ────────────────────────────────────────────────────────────

    /**
     * Returns the shared database connection, opening it if necessary.
     * Enables WAL journal mode (better concurrent read performance) and
     * foreign key enforcement on every new connection.
     *
     * @throws RuntimeException if the connection cannot be established
     */
    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DB_URL);
                try (Statement s = conn.createStatement()) {
                    s.execute("PRAGMA journal_mode=WAL");
                    s.execute("PRAGMA foreign_keys=ON");
                }
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    /** Closes the shared connection. Called from the App shutdown hook. */
    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException ignored) {}
    }

    // ── Schema ────────────────────────────────────────────────────────────────

    /**
     * Creates all application tables if they do not already exist.
     * Safe to call on every startup — uses CREATE TABLE IF NOT EXISTS.
     *
     * @throws RuntimeException if any table creation fails
     */
    public static void initSchema() {
        try (Statement s = getConnection().createStatement()) {
            s.execute("""
                    CREATE TABLE IF NOT EXISTS books (
                        book_id   TEXT PRIMARY KEY,
                        title     TEXT NOT NULL,
                        author    TEXT NOT NULL,
                        genre     TEXT NOT NULL,
                        available INTEGER NOT NULL DEFAULT 1
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS multimedia (
                        item_id   TEXT PRIMARY KEY,
                        title     TEXT NOT NULL,
                        type      TEXT NOT NULL,
                        duration  TEXT NOT NULL,
                        available INTEGER NOT NULL DEFAULT 1
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS members (
                        member_id TEXT PRIMARY KEY,
                        name      TEXT NOT NULL
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS member_borrowed_items (
                        member_id TEXT NOT NULL,
                        item_id   TEXT NOT NULL,
                        PRIMARY KEY (member_id, item_id)
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS borrow_records (
                        record_id   TEXT PRIMARY KEY,
                        member_id   TEXT NOT NULL,
                        item_id     TEXT NOT NULL,
                        borrow_date TEXT NOT NULL,
                        return_date TEXT NOT NULL DEFAULT '-',
                        returned    INTEGER NOT NULL DEFAULT 0
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS reservations (
                        reservation_id   TEXT PRIMARY KEY,
                        member_id        TEXT NOT NULL,
                        item_id          TEXT NOT NULL,
                        reservation_date TEXT NOT NULL,
                        active           INTEGER NOT NULL DEFAULT 1
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS fines (
                        fine_id     TEXT PRIMARY KEY,
                        member_id   TEXT NOT NULL,
                        item_id     TEXT NOT NULL,
                        days_late   INTEGER NOT NULL,
                        fine_amount INTEGER NOT NULL
                    )""");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize schema: " + e.getMessage(), e);
        }
    }

    /**
     * Returns true if the given table contains zero rows.
     * Used at startup to decide whether seed data needs to be inserted.
     *
     * @param table must be one of the known table names (validated against whitelist)
     * @throws IllegalArgumentException if the table name is not in the whitelist
     */
    public static boolean isTableEmpty(String table) {
        if (!KNOWN_TABLES.contains(table))
            throw new IllegalArgumentException("Unknown table: " + table);
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return !rs.next() || rs.getInt(1) == 0;
        } catch (SQLException e) {
            return true;
        }
    }

    // ── Books ─────────────────────────────────────────────────────────────────

    /**
     * Inserts a book row. Uses INSERT OR IGNORE so duplicate IDs are silently skipped
     * (safe to call during seed on every startup).
     */
    public static void insertBook(Book book) {
        String sql = "INSERT OR IGNORE INTO books (book_id, title, author, genre, available) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, book.getBookId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertBook failed: " + e.getMessage(), e);
        }
    }

    /** Deletes the book row with the given ID. */
    public static void deleteBook(String bookId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM books WHERE book_id=?")) {
            ps.setString(1, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteBook failed: " + e.getMessage(), e);
        }
    }

    /** Updates title, author, genre, and availability for the given book. */
    public static void updateBook(Book book) {
        String sql = "UPDATE books SET title=?, author=?, genre=?, available=? WHERE book_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getGenre());
            ps.setInt(4, book.isAvailable() ? 1 : 0);
            ps.setString(5, book.getBookId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateBook failed: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the available flag for any item — checks books first, then multimedia.
     * This single method works for both tables so callers do not need to know the item type.
     */
    public static void updateItemAvailability(String itemId, boolean available) {
        int val = available ? 1 : 0;
        try {
            int rows;
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE books SET available=? WHERE book_id=?")) {
                ps.setInt(1, val);
                ps.setString(2, itemId);
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                try (PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE multimedia SET available=? WHERE item_id=?")) {
                    ps.setInt(1, val);
                    ps.setString(2, itemId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("updateItemAvailability failed: " + e.getMessage(), e);
        }
    }

    /** Loads all book rows ordered by ID and returns them as a Book array. */
    public static Book[] loadBooks() {
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM books ORDER BY book_id")) {
            List<Book> list = new ArrayList<>();
            while (rs.next()) {
                Book b = new Book(rs.getString("book_id"), rs.getString("title"),
                        rs.getString("author"), rs.getString("genre"));
                b.setAvailable(rs.getInt("available") == 1);
                list.add(b);
            }
            return list.toArray(new Book[0]);
        } catch (SQLException e) {
            throw new RuntimeException("loadBooks failed: " + e.getMessage(), e);
        }
    }

    // ── Multimedia ────────────────────────────────────────────────────────────

    /** Inserts a multimedia row. Uses INSERT OR IGNORE for safe re-seeding. */
    public static void insertMultimedia(Multimedia item) {
        String sql = "INSERT OR IGNORE INTO multimedia (item_id, title, type, duration, available) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, item.getItemId());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getType());
            ps.setString(4, item.getDuration());
            ps.setInt(5, item.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertMultimedia failed: " + e.getMessage(), e);
        }
    }

    /** Loads all multimedia rows ordered by ID and returns them as a Multimedia array. */
    public static Multimedia[] loadMultimedia() {
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM multimedia ORDER BY item_id")) {
            List<Multimedia> list = new ArrayList<>();
            while (rs.next()) {
                Multimedia m = new Multimedia(rs.getString("item_id"), rs.getString("title"),
                        rs.getString("type"), rs.getString("duration"));
                m.setAvailable(rs.getInt("available") == 1);
                list.add(m);
            }
            return list.toArray(new Multimedia[0]);
        } catch (SQLException e) {
            throw new RuntimeException("loadMultimedia failed: " + e.getMessage(), e);
        }
    }

    // ── Members ───────────────────────────────────────────────────────────────

    /** Inserts a member row. Uses INSERT OR IGNORE for safe re-seeding. */
    public static void insertMember(Member member) {
        String sql = "INSERT OR IGNORE INTO members (member_id, name) VALUES (?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, member.getMemberId());
            ps.setString(2, member.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertMember failed: " + e.getMessage(), e);
        }
    }

    /** Deletes the member row with the given ID. */
    public static void deleteMember(String memberId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM members WHERE member_id=?")) {
            ps.setString(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteMember failed: " + e.getMessage(), e);
        }
    }

    /** Loads all member rows ordered by ID and returns them as a Member array. */
    public static Member[] loadMembers() {
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM members ORDER BY member_id")) {
            List<Member> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Member(rs.getString("member_id"), rs.getString("name")));
            }
            return list.toArray(new Member[0]);
        } catch (SQLException e) {
            throw new RuntimeException("loadMembers failed: " + e.getMessage(), e);
        }
    }

    // ── Member Borrowed Items ─────────────────────────────────────────────────

    /** Records that a member currently has an item on loan. */
    public static void insertMemberBorrowedItem(String memberId, String itemId) {
        String sql = "INSERT OR IGNORE INTO member_borrowed_items (member_id, item_id) VALUES (?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setString(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertMemberBorrowedItem failed: " + e.getMessage(), e);
        }
    }

    /** Removes the on-loan record when a member returns an item. */
    public static void deleteMemberBorrowedItem(String memberId, String itemId) {
        String sql = "DELETE FROM member_borrowed_items WHERE member_id=? AND item_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            ps.setString(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteMemberBorrowedItem failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all item IDs currently on loan to the given member.
     * Used at startup to restore each member's borrowedItems array.
     */
    public static String[] loadBorrowedItemIds(String memberId) {
        String sql = "SELECT item_id FROM member_borrowed_items WHERE member_id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getString("item_id"));
                return ids.toArray(new String[ids.size()]);
            }
        } catch (SQLException e) {
            throw new RuntimeException("loadBorrowedItemIds failed: " + e.getMessage(), e);
        }
    }

    // ── Borrow Records ────────────────────────────────────────────────────────

    /** Inserts a new borrow record row. Uses INSERT OR IGNORE to protect against duplicates. */
    public static void insertBorrowRecord(BorrowRecord record) {
        String sql = "INSERT OR IGNORE INTO borrow_records " +
                "(record_id, member_id, item_id, borrow_date, return_date, returned) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, record.getRecordId());
            ps.setString(2, record.getMember().getMemberId());
            ps.setString(3, record.getItem().getItemId());
            ps.setString(4, record.getBorrowDate());
            ps.setString(5, record.getReturnDate());
            ps.setInt(6, record.isReturned() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertBorrowRecord failed: " + e.getMessage(), e);
        }
    }

    /**
     * Sets returned=1 and records the return date for the most recent unreturned
     * borrow record matching the given member and item.
     */
    public static void markBorrowRecordReturned(String memberId, String itemId, String returnDate) {
        String sql = "UPDATE borrow_records SET return_date=?, returned=1 " +
                "WHERE member_id=? AND item_id=? AND returned=0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, returnDate);
            ps.setString(2, memberId);
            ps.setString(3, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("markBorrowRecordReturned failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads all borrow record rows as raw string arrays for startup reconstruction.
     * Row format: [record_id, member_id, item_id, borrow_date, return_date, returned(0/1)]
     */
    public static List<String[]> loadBorrowRecordRows() {
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM borrow_records ORDER BY record_id")) {
            List<String[]> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("record_id"),
                        rs.getString("member_id"),
                        rs.getString("item_id"),
                        rs.getString("borrow_date"),
                        rs.getString("return_date"),
                        String.valueOf(rs.getInt("returned"))
                });
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("loadBorrowRecordRows failed: " + e.getMessage(), e);
        }
    }

    // ── Reservations ──────────────────────────────────────────────────────────

    /** Inserts a new reservation row with active=1. */
    public static void insertReservation(String resId, String memberId, String itemId, String resDate) {
        String sql = "INSERT OR IGNORE INTO reservations " +
                "(reservation_id, member_id, item_id, reservation_date, active) VALUES (?,?,?,?,1)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, resId);
            ps.setString(2, memberId);
            ps.setString(3, itemId);
            ps.setString(4, resDate);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertReservation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads all reservation rows as raw string arrays for startup reconstruction.
     * Row format: [reservation_id, member_id, item_id, reservation_date, active(0/1)]
     */
    public static List<String[]> loadReservationRows() {
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM reservations ORDER BY reservation_id")) {
            List<String[]> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("reservation_id"),
                        rs.getString("member_id"),
                        rs.getString("item_id"),
                        rs.getString("reservation_date"),
                        String.valueOf(rs.getInt("active"))
                });
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("loadReservationRows failed: " + e.getMessage(), e);
        }
    }

    // ── Fines ─────────────────────────────────────────────────────────────────

    /** Inserts a new fine row. Uses INSERT OR IGNORE to protect against duplicates. */
    public static void insertFine(Fine fine) {
        String sql = "INSERT OR IGNORE INTO fines (fine_id, member_id, item_id, days_late, fine_amount) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, fine.getFineId());
            ps.setString(2, fine.getMember().getMemberId());
            ps.setString(3, fine.getItem().getItemId());
            ps.setInt(4, fine.getDaysLate());
            ps.setInt(5, fine.getFineAmount());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("insertFine failed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads all fine rows as raw string arrays for startup reconstruction.
     * Row format: [fine_id, member_id, item_id, days_late]
     */
    public static List<String[]> loadFineRows() {
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM fines ORDER BY fine_id")) {
            List<String[]> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("fine_id"),
                        rs.getString("member_id"),
                        rs.getString("item_id"),
                        String.valueOf(rs.getInt("days_late"))
                });
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("loadFineRows failed: " + e.getMessage(), e);
        }
    }
}
