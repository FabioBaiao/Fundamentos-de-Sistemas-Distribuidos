package bookstore;

import java.util.concurrent.ExecutionException;

public interface Book{
    public int getIsbn() throws Exception;
    public String getTitle() throws Exception;
    public String getAuthor() throws Exception;
}
