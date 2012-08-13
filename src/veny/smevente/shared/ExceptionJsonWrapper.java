package veny.smevente.shared;


/**
 * Exception wrapper thrown from the server when using ClientRestHandler.
 *
 * @author Vaclav Sykora [vaclav.sykora@gmail.com]
 * @since 0.1
 */
public class ExceptionJsonWrapper {

    /** Exception message. */
    private String message;

    /** Exception class name. */
    private String className;

    /** Exception ID. */
    private String id;

    /** Exception cause, if filled. */
    private Throwable cause;

    /**
     * Empty constructor.
     */
    public ExceptionJsonWrapper() {
        // nothing to do here
    }

    /**
     * Constructor copies data from the cause Throwable.
     * @param ex the cause Throwable to copy data from
     */
    public ExceptionJsonWrapper(final Throwable ex) {
        this.message = ex.getMessage();
        this.className = ex.getClass().getName();
        this.cause = ex;
    }

    /**
     * Getter for the exception message.
     * @return the exception message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for the exception message.
     * @param message the exception message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Getter for the exception class name.
     * @return the exception class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Setter for the exception class name.
     * @param className the exception class name
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * Getter for the exception cause.
     * @return the exception cause
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Setter for the exception cause.
     * @param cause the exception cause
     */
    public void setCause(final Throwable cause) {
        this.cause = cause;
    }

    /**
     * Gets exception ID.
     * @return exception ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets exception ID.
     * @param id exception ID
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets flag whether the exception is a validation problem.
     * @return <i>true</i> if the exception is a validation problem
     */
    public boolean isValidation() {
        return (null != getClassName() && getClassName().endsWith("ValidationException"));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder("ExceptionJsonWrapper(id=")
            .append(getId())
            .append(", className='")
            .append(className)
            .append(", message='")
            .append(message)
            .append("')")
            .toString();
    }

}
