package nyc.c4q.jonathancolon.inContaq.contactlist;


import java.util.concurrent.ExecutionException;

public interface AlertDialogCallback<T> {
    void alertDialogCallback(T ret) throws ExecutionException, InterruptedException;
}
