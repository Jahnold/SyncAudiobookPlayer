package com.jahnold.syncaudiobookplayer.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("BookPath")
public class BookPath extends ParseObject {

    // getters
    public String getPath() { return getString("path"); }
    public String getInstallId() { return getString("installId"); }
    public Book getBook() { return  (Book) getParseObject("book"); }

    // setters
    public void setPath(String path) { put("path", path);}
    public void setInstallId(String id) { put("installId", id);}
    public void setBook(Book book) { put("book", book); }

}
