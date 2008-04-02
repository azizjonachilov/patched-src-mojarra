/*
 * $Id: ConfigValidator.java,v 1.2 2004/02/04 23:39:34 ofung Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces.webapp;


/**
 * <p>Config Bean for a Validator.</p>
 */
public class ConfigValidator extends ConfigFeature {

    private String validatorId;
    public String getValidatorId() {
        return (this.validatorId);
    }
    public void setValidatorId(String validatorId) {
        this.validatorId = validatorId;
    }

    private String validatorClass;
    public String getValidatorClass() {
        return (this.validatorClass);
    }
    public void setValidatorClass(String validatorClass) {
        this.validatorClass = validatorClass;
    }

}
