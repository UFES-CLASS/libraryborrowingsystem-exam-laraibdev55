/**
 * @author      masjohncook
 * @version     0.0.1
 * @copyright   (C) Copyright 2026
 * @license     None
 * @maintainer  masjohncook
 * @email       mas.john.cook@gmail.com
 * @status      Development
 */
package com.fad.LibrarySystem.model;

/**
 * Represents a single physical book in the library collection.
 * Inherits from LibraryItem — gains itemId, title, and available.
 *
 * A Book is a specific type of LibraryItem that adds two extra
 * attributes: the author who wrote it and the genre it belongs to.
 * It also provides the seed data loaded into the database at first startup.
 *
 * Inheritance:
 *   Book extends LibraryItem
 *   - Inherits : itemId, title, available (and their getters/setters)
 *   - Adds     : author, genre
 *   - Overrides: getInfo(), toString()
 *
 * Attributes:
 *   - author : name of the book's author
 *   - genre  : category of the book (e.g. "Fiction", "Classic", "Sci-Fi")
 */
public class Book extends LibraryItem {
    
    private String author;
    private  String genre;
    public String getGenre(){
        return genre;

    }
    public String getAuthor(){
            return author;

    }
        
    public Book(String bookId, String title, String author, String genre) {
        super(bookId, title);
    
    
        this.author = author;
        this.genre  = genre;
    }


    /**
     * Returns the full set of seed books inserted into the database on first startup.
     * Uses INSERT OR IGNORE so re-running on an existing database is safe.
     *
     * @return array of Book objects covering a range of genres
     */
    public static Book[] getInitialBooks() {
        return new Book[]{
            // ── Original ──────────────────────────────────────────────────────
            new Book("B001", "The Great Gatsby",        "F. Scott Fitzgerald", "Classic"),
            new Book("B002", "To Kill a Mockingbird",   "Harper Lee",          "Fiction"),
            new Book("B003", "1984",                    "George Orwell",       "Dystopian"),
            new Book("B004", "Brave New World",         "Aldous Huxley",       "Sci-Fi"),
            new Book("B005", "The Catcher in the Rye",  "J.D. Salinger",       "Fiction"),
            // ── Fiction ───────────────────────────────────────────────────────
            new Book("B006", "Pride and Prejudice",          "Jane Austen",         "Fiction"),
            new Book("B007", "Jane Eyre",                    "Charlotte Bronte",    "Fiction"),
            new Book("B008", "Wuthering Heights",            "Emily Bronte",        "Fiction"),
            new Book("B009", "The Old Man and the Sea",      "Ernest Hemingway",    "Fiction"),
            new Book("B010", "Of Mice and Men",              "John Steinbeck",      "Fiction"),
            new Book("B011", "East of Eden",                 "John Steinbeck",      "Fiction"),
            new Book("B012", "The Grapes of Wrath",          "John Steinbeck",      "Fiction"),
            new Book("B013", "Beloved",                      "Toni Morrison",       "Fiction"),
            new Book("B014", "The Color Purple",             "Alice Walker",        "Fiction"),
            new Book("B015", "Their Eyes Were Watching God", "Zora Neale Hurston",  "Fiction"),
            new Book("B016", "Gone with the Wind",           "Margaret Mitchell",   "Fiction"),
            new Book("B017", "Rebecca",                      "Daphne du Maurier",   "Fiction"),
            new Book("B018", "The Bell Jar",                 "Sylvia Plath",        "Fiction"),
            new Book("B019", "Mrs Dalloway",                 "Virginia Woolf",      "Fiction"),
            new Book("B020", "To the Lighthouse",            "Virginia Woolf",      "Fiction"),
            // ── Classic ───────────────────────────────────────────────────────
            new Book("B021", "Crime and Punishment",           "Fyodor Dostoevsky",   "Classic"),
            new Book("B022", "War and Peace",                  "Leo Tolstoy",         "Classic"),
            new Book("B023", "Anna Karenina",                  "Leo Tolstoy",         "Classic"),
            new Book("B024", "Don Quixote",                    "Miguel de Cervantes", "Classic"),
            new Book("B025", "Moby Dick",                      "Herman Melville",     "Classic"),
            new Book("B026", "Adventures of Huckleberry Finn", "Mark Twain",          "Classic"),
            new Book("B027", "The Count of Monte Cristo",      "Alexandre Dumas",     "Classic"),
            new Book("B028", "Les Miserables",                 "Victor Hugo",         "Classic"),
            new Book("B029", "Frankenstein",                   "Mary Shelley",        "Classic"),
            new Book("B030", "Dracula",                        "Bram Stoker",         "Classic"),
            new Book("B031", "Oliver Twist",                   "Charles Dickens",     "Classic"),
            new Book("B032", "Great Expectations",             "Charles Dickens",     "Classic"),
            new Book("B033", "A Tale of Two Cities",           "Charles Dickens",     "Classic"),
            new Book("B034", "David Copperfield",              "Charles Dickens",     "Classic"),
            new Book("B035", "Middlemarch",                    "George Eliot",        "Classic"),
            // ── Sci-Fi ────────────────────────────────────────────────────────
            new Book("B036", "Dune",                                    "Frank Herbert",      "Sci-Fi"),
            new Book("B037", "Foundation",                              "Isaac Asimov",       "Sci-Fi"),
            new Book("B038", "The Hitchhiker's Guide to the Galaxy",    "Douglas Adams",      "Sci-Fi"),
            new Book("B039", "Ender's Game",                            "Orson Scott Card",   "Sci-Fi"),
            new Book("B040", "The Martian",                             "Andy Weir",          "Sci-Fi"),
            new Book("B041", "Fahrenheit 451",                          "Ray Bradbury",       "Sci-Fi"),
            new Book("B042", "I Robot",                                 "Isaac Asimov",       "Sci-Fi"),
            new Book("B043", "Neuromancer",                             "William Gibson",     "Sci-Fi"),
            new Book("B044", "The Left Hand of Darkness",               "Ursula K. Le Guin", "Sci-Fi"),
            new Book("B045", "Childhood's End",                         "Arthur C. Clarke",   "Sci-Fi"),
            new Book("B046", "The War of the Worlds",                   "H.G. Wells",         "Sci-Fi"),
            new Book("B047", "The Time Machine",                        "H.G. Wells",         "Sci-Fi"),
            new Book("B048", "Do Androids Dream of Electric Sheep",     "Philip K. Dick",     "Sci-Fi"),
            new Book("B049", "Slaughterhouse-Five",                     "Kurt Vonnegut",      "Sci-Fi"),
            new Book("B050", "The Handmaid's Tale",                     "Margaret Atwood",    "Dystopian"),
        };
    }

    @Override
    public String getInfo() {
        String status = available ? "Available" : "Borrowed";
        return "[" + itemId + "] \"" + title + "\" by " + author
                + " | Genre: " + genre + " (" + status + ")";
    }

    @Override
    public String toString() { return getInfo(); }

    public String getBookId() { return itemId; }

    public void setBookId(String bookId) { this.itemId = bookId; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre)   { this.genre  = genre; }
}
