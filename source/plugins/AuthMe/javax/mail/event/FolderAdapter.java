package javax.mail.event;

public abstract class FolderAdapter implements FolderListener {
   public FolderAdapter() {
      super();
   }

   public void folderCreated(FolderEvent e) {
   }

   public void folderRenamed(FolderEvent e) {
   }

   public void folderDeleted(FolderEvent e) {
   }
}
