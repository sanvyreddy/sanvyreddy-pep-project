package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages",
                this::getMessagesByAccountId);

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void registerAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Account registeredAccount = accountService.createAccount(account);

            
            ctx.json(mapper.writeValueAsString(registeredAccount));
        } catch (ServiceException e) {
            
            ctx.status(400);
        }
    }
    
    private void loginAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); 
                                                  
        Account account = mapper.readValue(ctx.body(), Account.class);

        try {
            Optional<Account> loggedInAccount = accountService
                    .validateLogin(account);
            if (loggedInAccount.isPresent()) {
                
                ctx.json(mapper.writeValueAsString(loggedInAccount));
                ctx.sessionAttribute("logged_in_account",
                        loggedInAccount.get());
                ctx.json(loggedInAccount.get());
            } else {
                
                ctx.status(401);
            }
        } catch (ServiceException e) {
            
            ctx.status(401);
        }
    }

    private void createMessage(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            Optional<Account> account = accountService
                    .getAccountById(mappedMessage.getPosted_by());
            Message message = messageService.createMessage(mappedMessage,
                    account);
            ctx.json(message);
        } catch (ServiceException e) {
            
            ctx.status(400);
        }
    }

    private void getAllMessages(Context ctx) {

        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    private void getMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.json(message.get());
            } else {
                
                ctx.status(200); 
                                 
                ctx.result(""); 
            }
            
        } catch (NumberFormatException e) {
            ctx.status(400); 
        } catch (ServiceException e) {
            ctx.status(200); 
            ctx.result(""); 
        }
    }

    private void deleteMessageById(Context ctx) {
        try {
            
            int id = Integer.parseInt(ctx.pathParam("message_id"));

            
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                
                messageService.deleteMessage(message.get());
                ctx.status(200);
                
                ctx.json(message.get());
            } else {
                
                ctx.status(200);
            }
        } catch (ServiceException e) {
            
            ctx.status(200);
        }
    }

    private void updateMessageById(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            mappedMessage.setMessage_id(id);

            
            Message messageUpdated = messageService
                    .updateMessage(mappedMessage);

            
            ctx.json(messageUpdated);

        } catch (ServiceException e) {
            
            ctx.status(400);
        }
    }

    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));

           
            List<Message> messages = messageService
                    .getMessagesByAccountId(accountId);
            if (!messages.isEmpty()) {
                
                ctx.json(messages);
            } else {
                
                ctx.json(messages);
                ctx.status(200);
            }
        } catch (ServiceException e) {
            
            ctx.status(400);
        }
    }
}