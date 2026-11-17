package application;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

interface IDigital {
    String printCommunicationMethod();
}

enum PriorityType {
    REGULAR,
    URGENT,
    LOW
}

class AttachmentException extends Exception {
    public AttachmentException(String message) {
        super(message);
    }
}

class InvalidPhoneNumberException extends Exception {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}

class File {
    private String fileName;
    private String fileType;

    public File(String fileName, String fileType) {
        setFileName(fileName);
        setFileType(fileType);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new IllegalArgumentException("File type cannot be null or empty.");
        }
        this.fileType = fileType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        File other = (File) obj;
        return Objects.equals(fileName, other.fileName) &&
               Objects.equals(fileType, other.fileType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileType);
    }

    @Override
    public String toString() {
        return fileName + "." + fileType;
    }
}

abstract class Message {
    protected String sender;
    protected String content;
    protected Date sendDate;
    protected boolean isRead;

    public Message(String sender, String content, Date sendDate, boolean isRead) {
        setSender(sender);
        setContent(content);
        if (sendDate == null) {
            throw new IllegalArgumentException("Send date cannot be null.");
        }
        this.sendDate = sendDate;
        this.isRead = isRead;
    }

    public Message(String sender, String content, boolean isRead) {
        this(sender, content, new Date(), isRead);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender cannot be null or empty.");
        }
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty.");
        }
        this.content = content;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean find(ArrayList<String> words) {
        if (words == null || words.isEmpty()) {
            return false;
        }
        String lowerContent = this.content.toLowerCase();
        for (String word : words) {
            if (word != null && !word.isEmpty() && lowerContent.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public int getWordCount() {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }

    public abstract String generatePreview();

    @Override
    public String toString() {
        return "Sender: " + sender +
               " | Date: " + sendDate +
               " | Read: " + (isRead ? "Yes" : "No") +
               " | Content: " + content;
    }
}

class BoardMessage extends Message {
    private PriorityType priority;
    private String location;

    public BoardMessage(String sender, String content, Date sendDate, boolean isRead, PriorityType priority, String location) {
        super(sender, content, sendDate, isRead);
        setPriority(priority);
        setLocation(location);
    }

    public BoardMessage(String sender, String content, boolean isRead, String location) {
        this(sender, content, new Date(), isRead, PriorityType.REGULAR, location);
    }

    public PriorityType getPriority() {
        return priority;
    }

    public void setPriority(PriorityType priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null.");
        }
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty.");
        }
        this.location = location;
    }

    public boolean isUrgent() {
        return this.priority == PriorityType.URGENT;
    }

    @Override
    public String generatePreview() {
        String shortContent = content.length() <= 15 ? content : content.substring(0, 15) + "...";
        return "[Board] " + sender + ": " + shortContent;
    }

    @Override
    public String toString() {
        return "[BoardMessage] " + super.toString() +
               " | Priority: " + priority +
               " | Location: " + location;
    }
}

class EmailMessage extends Message implements IDigital {
    private String subject;
    private ArrayList<File> attachments;

    public EmailMessage(String sender, String content, Date sendDate, boolean isRead, String subject, ArrayList<File> attachments) {
        super(sender, content, sendDate, isRead);
        setSubject(subject);
        if (attachments == null) {
            this.attachments = new ArrayList<>();
        } else {
            this.attachments = new ArrayList<>(attachments);
        }
    }

    public EmailMessage(String sender, String content, boolean isRead, String subject) {
        this(sender, content, new Date(), isRead, subject, new ArrayList<>());
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty.");
        }
        this.subject = subject;
    }

    public ArrayList<File> getAttachments() {
        return attachments;
    }

    public void addAttachment(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Attachment file cannot be null.");
        }
        attachments.add(file);
    }

    public void removeAttachment(File file) throws AttachmentException {
        if (file == null) {
            throw new IllegalArgumentException("File to remove cannot be null.");
        }
        boolean removed = attachments.removeIf(f -> f.equals(file));
        if (!removed) {
            throw new AttachmentException("Attachment '" + file + "' was not found in the email.");
        }
    }

    @Override
    public String printCommunicationMethod() {
        return "Sent via Email Server";
    }

    @Override
    public String generatePreview() {
        return "[Email] Subject: " + subject + " | From: " + sender;
    }

    @Override
    public String toString() {
        return "[EmailMessage] " + super.toString() +
               " | Subject: " + subject +
               " | Attachments: " + attachments;
    }
}

class SmsMessage extends Message implements IDigital {
    private String phoneNumber;

    public SmsMessage(String sender, String content, Date sendDate, boolean isRead, String phoneNumber) throws InvalidPhoneNumberException {
        super(sender, content, sendDate, isRead);
        setPhoneNumber(phoneNumber);
    }

    public SmsMessage(String sender, String content, boolean isRead, String phoneNumber) throws InvalidPhoneNumberException {
        this(sender, content, new Date(), isRead, phoneNumber);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) throws InvalidPhoneNumberException {
        if (phoneNumber == null || !phoneNumber.matches("05\\d{8}")) {
            throw new InvalidPhoneNumberException("Invalid phone number: must be 10 digits starting with '05'.");
        }
        this.phoneNumber = phoneNumber;
    }

    public double calculateCost() {
        return Math.ceil(content.length() / 70.0) * 0.5;
    }

    @Override
    public String printCommunicationMethod() {
        return "Sent via Cellular SMS Gateway";
    }

    @Override
    public String generatePreview() {
        String shortContent = content.length() <= 15 ? content : content.substring(0, 15) + "...";
        return "[SMS] From: " + sender + " (" + phoneNumber + "): " + shortContent;
    }

    @Override
    public String toString() {
        return "[SmsMessage] " + super.toString() +
               " | Phone Number: " + phoneNumber;
    }
}

public class Main {
    private static ArrayList<Message> messages = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initDefaultMessages();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addMessageMenu();
                    break;
                case "2":
                    deleteMessageMenu();
                    break;
                case "3":
                    printAllMessages();
                    break;
                case "4":
                    countMessagesWithWords();
                    break;
                case "5":
                    printDigitalMessagesOnly();
                    break;
                case "6":
                    printAllPreviews();
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 0 and 6.");
            }
            System.out.println();
        }
    }

    private static void initDefaultMessages() {
        try {
            messages.add(new BoardMessage("Dana", "Important update regarding the final exam schedule", false, "Building A - Floor 2"));
            messages.add(new BoardMessage("Admin", "Water outage tomorrow between 09:00 and 12:00", new Date(), true, PriorityType.URGENT, "Main Entrance"));

            EmailMessage email1 = new EmailMessage("Noa", "Attached is the homework assignment for Java course.", false, "Project Submission");
            email1.addAttachment(new File("HW1", "pdf"));
            email1.addAttachment(new File("Main", "java"));
            messages.add(email1);

            EmailMessage email2 = new EmailMessage("Yossi", "Please review the budget report before Monday.", true, "Q1 Budget Report");
            email2.addAttachment(new File("budget_2026", "xlsx"));
            messages.add(email2);

            messages.add(new SmsMessage("Mom", "Call me when you finish class please!", false, "0501234567"));
            messages.add(new SmsMessage("Delivery", "Your package has arrived at the pickup point.", true, "0529876543"));

        } catch (Exception e) {
            System.out.println("Error initializing default messages: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("=== Messaging System Menu ===");
        System.out.println("1. Add a new message");
        System.out.println("2. Delete a message");
        System.out.println("3. Print all messages");
        System.out.println("4. Count messages containing specific words");
        System.out.println("5. Print digital messages only");
        System.out.println("6. Print previews of all messages");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    private static void addMessageMenu() {
        try {
            System.out.println("Select message type to add:");
            System.out.println("1 - Board Message");
            System.out.println("2 - Email Message");
            System.out.println("3 - SMS Message");
            System.out.print("Your choice: ");
            String type = scanner.nextLine().trim();

            System.out.print("Enter sender name: ");
            String sender = scanner.nextLine();

            System.out.print("Enter message content: ");
            String content = scanner.nextLine();

            switch (type) {
                case "1":
                    System.out.print("Enter board location: ");
                    String location = scanner.nextLine();
                    System.out.print("Enter priority (REGULAR, URGENT, LOW): ");
                    PriorityType priority = PriorityType.valueOf(scanner.nextLine().trim().toUpperCase());
                    messages.add(new BoardMessage(sender, content, new Date(), false, priority, location));
                    System.out.println("Board message added successfully!");
                    break;

                case "2":
                    System.out.print("Enter email subject: ");
                    String subject = scanner.nextLine();
                    EmailMessage email = new EmailMessage(sender, content, false, subject);
                    System.out.print("How many attachments would you like to add? ");
                    int count = Integer.parseInt(scanner.nextLine().trim());
                    for (int i = 0; i < count; i++) {
                        System.out.print("Enter file #" + (i + 1) + " name: ");
                        String fName = scanner.nextLine();
                        System.out.print("Enter file #" + (i + 1) + " type (e.g. pdf, docx): ");
                        String fType = scanner.nextLine();
                        email.addAttachment(new File(fName, fType));
                    }
                    messages.add(email);
                    System.out.println("Email message added successfully!");
                    break;

                case "3":
                    System.out.print("Enter phone number (10 digits starting with 05): ");
                    String phone = scanner.nextLine();
                    messages.add(new SmsMessage(sender, content, false, phone));
                    System.out.println("SMS message added successfully!");
                    break;

                default:
                    System.out.println("Error: Invalid message type selected.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Validation Error: " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            System.out.println("Phone Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error adding message: " + e.getMessage());
        }
    }

    private static void deleteMessageMenu() {
        if (messages.isEmpty()) {
            System.out.println("No messages available to delete.");
            return;
        }
        printAllMessages();
        System.out.print("Enter the index of the message to delete (0 to " + (messages.size() - 1) + "): ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim());
            if (index < 0 || index >= messages.size()) {
                System.out.println("Error: Index out of bounds.");
            } else {
                Message removed = messages.remove(index);
                System.out.println("Message deleted successfully: " + removed.generatePreview());
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid integer index.");
        }
    }

    private static void printAllMessages() {
        if (messages.isEmpty()) {
            System.out.println("The message repository is empty.");
            return;
        }
        System.out.println("--- All Messages ---");
        for (int i = 0; i < messages.size(); i++) {
            System.out.println("[" + i + "] " + messages.get(i));
        }
    }

    private static void countMessagesWithWords() {
        System.out.print("Enter words separated by spaces to search for: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("No words entered.");
            return;
        }

        ArrayList<String> words = new ArrayList<>();
        for (String w : input.split("\\s+")) {
            words.add(w);
        }

        int matchCount = 0;
        for (Message msg : messages) {
            if (msg.find(words)) {
                matchCount++;
            }
        }
        System.out.println("Number of messages containing at least one of the words: " + matchCount);
    }

    private static void printDigitalMessagesOnly() {
        System.out.println("--- Digital Messages ---");
        boolean found = false;
        for (Message msg : messages) {
            if (msg instanceof IDigital) {
                IDigital digitalMsg = (IDigital) msg;
                System.out.println(msg + " | Method: " + digitalMsg.printCommunicationMethod());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No digital messages found.");
        }
    }

    private static void printAllPreviews() {
        if (messages.isEmpty()) {
            System.out.println("No messages to preview.");
            return;
        }
        System.out.println("--- Message Previews ---");
        for (Message msg : messages) {
            System.out.println(msg.generatePreview() + " (Word count: " + msg.getWordCount() + ")");
        }
    }
}

