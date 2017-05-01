package com.lexmark.example.docwriter;


public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
   /**
    * @see RuntimeException(<code>String</code>)
    */
    public ServiceException(String message) {
        super(message);
    }
    
    /**
     * @see RuntimeException(<code>String</code>, <code>Throwable</code>)
     */
    public ServiceException(String message, Exception throwable) {
        super(message + throwable);
    }
    
    /**
     * @see RuntimeException(<code>Throwable</code>)
     */
    public ServiceException(Exception throwable) {
        super(throwable.getMessage());
    }

}
