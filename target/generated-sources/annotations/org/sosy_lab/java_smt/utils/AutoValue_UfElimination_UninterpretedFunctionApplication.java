package org.sosy_lab.java_smt.utils;

import com.google.common.collect.ImmutableList;
import javax.annotation.processing.Generated;
import org.sosy_lab.java_smt.api.Formula;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_UfElimination_UninterpretedFunctionApplication extends UfElimination.UninterpretedFunctionApplication {

  private final Formula formula;

  private final ImmutableList<Formula> arguments;

  private final Formula substitution;

  AutoValue_UfElimination_UninterpretedFunctionApplication(
      Formula formula,
      ImmutableList<Formula> arguments,
      Formula substitution) {
    if (formula == null) {
      throw new NullPointerException("Null formula");
    }
    this.formula = formula;
    if (arguments == null) {
      throw new NullPointerException("Null arguments");
    }
    this.arguments = arguments;
    if (substitution == null) {
      throw new NullPointerException("Null substitution");
    }
    this.substitution = substitution;
  }

  @Override
  Formula getFormula() {
    return formula;
  }

  @Override
  ImmutableList<Formula> getArguments() {
    return arguments;
  }

  @Override
  Formula getSubstitution() {
    return substitution;
  }

  @Override
  public String toString() {
    return "UninterpretedFunctionApplication{"
        + "formula=" + formula + ", "
        + "arguments=" + arguments + ", "
        + "substitution=" + substitution
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof UfElimination.UninterpretedFunctionApplication) {
      UfElimination.UninterpretedFunctionApplication that = (UfElimination.UninterpretedFunctionApplication) o;
      return this.formula.equals(that.getFormula())
          && this.arguments.equals(that.getArguments())
          && this.substitution.equals(that.getSubstitution());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= formula.hashCode();
    h$ *= 1000003;
    h$ ^= arguments.hashCode();
    h$ *= 1000003;
    h$ ^= substitution.hashCode();
    return h$;
  }

}
