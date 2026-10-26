package de.szut.pms.customer;

/** Wird geworfen, wenn es keinen Kunden mit dieser Id gibt → HTTP 404. */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Es gibt keinen Kunden mit der Id " + id + ".");
    }
}
