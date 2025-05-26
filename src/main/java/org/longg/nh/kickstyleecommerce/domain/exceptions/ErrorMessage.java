package org.longg.nh.kickstyleecommerce.domain.exceptions;

import com.eps.shared.models.exceptions.BaseErrorMessage;

public enum ErrorMessage implements BaseErrorMessage {
  ;

  public String val;

  private ErrorMessage(String label) {
    val = label;
  }

  @Override
  public String val() {
    return val;
  }
}
