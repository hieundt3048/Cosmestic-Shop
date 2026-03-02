package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.CreateUserRequest;

/**
 * Abstract cho chuỗi xác thực
 */
public abstract class AbstractValidationHandler implements ValidationHandler {
    
    private ValidationHandler next;
    
    @Override
    public void setNext(ValidationHandler next) {
        this.next = next;
    }
    
    protected void validateNext(CreateUserRequest request) {
        if (next != null) {
            next.validate(request);
        }
    }
    
    @Override
    public abstract void validate(CreateUserRequest request);
}
