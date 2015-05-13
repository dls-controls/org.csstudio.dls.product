package org.csstudio.logbook.dls;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import oracle.jdbc.OracleTypes;
import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.csstudio.platform.utility.rdb.RDBUtil;

public class ELog {

    private final RDBUtil rdb;
    public final static String[] IMAGE_TYPES = {"png", "ico", "gif", "jpg", "jpeg", "bmp", "tif"};
    private static ArrayList<String> supportedExtensions; 

    private enum AttachmentType {
        ATTACHMENT, IMAGE
    }

    public ELog() throws Exception {
        rdb = RDBUtil.connect(Preferences.getURL(), Preferences.getLogListUser(), Preferences.getLogListPassword(), false);
    };

    public ArrayList<String> getLogbooks() {
        final ArrayList<String> names = new ArrayList<String>();
        // Just try everything, we want a stack trace if something doesn't work anyway
        try {
            final Statement statement = rdb.getConnection().createStatement();
            final ResultSet result = statement.executeQuery("SELECT logbook_name FROM cs_log_logbook ORDER BY logbook_name");
            while (result.next()) {
                names.add(result.getString(1));
            }
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
    }

    public boolean isExtensionSupported(String fileExtension) throws Exception {
        // Cache extensions from database
        if(supportedExtensions == null) {
            final Statement statement = rdb.getConnection().createStatement();
            final String query = "select * from cs_log_attachmenttype";
            ResultSet result = statement.executeQuery(query);

            // Don't Initialise early in case an exception is thrown
            supportedExtensions = new ArrayList<>();
            while(result.next()) {
                supportedExtensions.add(result.getString("file_extension"));
            }
        }

        // Check cached strings for existence
        for(String ext : supportedExtensions) {
            if(fileExtension.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    public void createEntry(String username, String password, String title, String text, String logbook, ArrayList<String> fileNames) throws Exception {
        if(username.length() == 0) {
            throw new Exception("You must provide a username.");
        }
        if(password.length() == 0) {
            throw new Exception("You must provide a password");
        }

        if(!isLdapAuthenticated(username,password)) {
            throw new Exception("Authentication Failed");
        }

        if(!checkUserPermission(username, logbook)) {
            throw new Exception("Don't have write permission to log");
        }

        int textEntryId = createTextEntry(username, title, text, logbook);

        for(String fileName : fileNames) {
            addAttachment(username, textEntryId, fileName);
        }
    }

    private int createTextEntry(String username, String title, String text, String logbook) throws Exception {
        int entryId = -1;
        
        // Replace line endings for HTML
        text = text.replace("\n","<br />");

        // Initiate the multi-file sql and retrieve the entry_id
        final String mysql = "call elog_pkg.insert_logbook_entry (?, ?, ?, ?, ?)";
        CallableStatement statement = rdb.getConnection().prepareCall(mysql);

        try {
            // Set data
            statement.setString(1, username);
            statement.setString(2, logbook);
            statement.setString(3, title);
            final CLOB clob = CLOB.createTemporary(rdb.getConnection(), true, CLOB.DURATION_SESSION);
            clob.setString(4, text);
            statement.setClob(4, clob);
            statement.registerOutParameter(5, OracleTypes.NUMBER);

            // Execute
            statement.executeQuery();

            // Return entry ID for adding attachments
            entryId = Integer.parseInt(statement.getString(5));
        } finally {
            statement.close();
        }
        return entryId;
    }

    private  void addAttachment(String username, int entryId, String fileName) throws Exception {
        // Get the file type ID from the RDB, if the extension isn't recognised throw an exception
        final String fileExtension = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
        long fileTypeId = getFileTypeId(fileExtension);
        if(fileTypeId == -1) {
            throw new Exception("File extension " + fileExtension + " is not recognised, the file has not been added.");
        }

        // Determine attachment type from file extension
        AttachmentType attachmentType = AttachmentType.ATTACHMENT;
        for(String ext : IMAGE_TYPES) {
            if(ext.equalsIgnoreCase(fileExtension)) {
                attachmentType = AttachmentType.IMAGE;
            }
        }

        final String mysql = "call elog_pkg.add_entry_attachment(?, ?, ?, ?, ?, ?, ?, ?)";
        final CallableStatement statement = rdb.getConnection().prepareCall(mysql);
        try    {
            statement.setInt(1, entryId);
            statement.setString(2, fileExtension);
            statement.setString(3, fileName);
            statement.setLong(4, fileTypeId);
            statement.setString(6, username);
            statement.registerOutParameter(7, OracleTypes.NUMBER);

            final File inputFile = new File(fileName);
            // RDB file types are 'I' for image and 'A' for attachment
            switch (attachmentType) {
            case IMAGE:
                statement.setString(5, "I");    
                final int fileSize = (int) inputFile.length();
                final FileInputStream inputStream = new FileInputStream(inputFile);
                statement.setBinaryStream(8, inputStream, fileSize);
                inputStream.close();
                break;
            case ATTACHMENT:
                statement.setString(5, "A");
                final BLOB blob = BLOB.createTemporary(rdb.getConnection(), true, BLOB.DURATION_SESSION);
                blob.setBytes( 1L, Files.readAllBytes(inputFile.toPath()));
                statement.setBlob(8, blob);
                break;
            }
            statement.executeQuery();

        } finally {
            statement.close();
        }
    }

    private long getFileTypeId(String fileExtension) throws Exception {
        final Statement statement = rdb.getConnection().createStatement();
        final String query = "select * from cs_log_attachmenttype";
        try {
            ResultSet result = statement.executeQuery(query);
            while(result.next()) {
                final long ID = result.getLong("attachment_type_id");
                final String extension = result.getString("file_extension");
                if(fileExtension.equalsIgnoreCase(extension)) {
                    return ID;
                }
            }
        }
        finally {
            statement.close();
        }
        return -1;
    }

    private boolean isLdapAuthenticated(final String fedid,final String password) throws Exception {    
        Hashtable <String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://130.246.132.94:389");

        // Authenticate using fedid and password
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn="+fedid+",ou=DLS,dc=fed,dc=cclrc,dc=ac,dc=uk");
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext ctx = new InitialDirContext(env);
            ctx.close();
            return true;            
        } catch (NamingException e) {
            return false ;
        }
    }

    private boolean checkUserPermission(final String fed_id, final String logbook) throws Exception, SQLException {
        final PreparedStatement statement = rdb.getConnection()
                .prepareStatement("SELECT  insert_flag "+
                        "FROM    cs_ger_userlogbookaccess a, " +
                        "cs_log_logbook b "+
                        "WHERE  b.logbook_id        = a.logbook_id " +                                   
                        "AND    lower(user_id)      = lower('" + fed_id.trim() +"') " +
                        "AND    lower(logbook_name) = lower('" + logbook.trim() +"') ");    

        try {
            statement.execute();
            final ResultSet result = statement.getResultSet();

            if (result.next()) {
                final String permission = result.getString(1);
                if (permission.equals("Y") )
                    return true;
                else
                    return false;
            }
        }
        finally {
            statement.close();
        }
        return false;
    }       

}