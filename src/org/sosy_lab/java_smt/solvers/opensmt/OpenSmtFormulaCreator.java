// This file is part of JavaSMT,
// an API wrapper for a collection of SMT solvers:
// https://github.com/sosy-lab/java-smt
//
// SPDX-FileCopyrightText: 2020 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

package org.sosy_lab.java_smt.solvers.opensmt;

import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
import java.util.List;
import opensmt.ArithLogic;
import opensmt.Logic;
import opensmt.LogicFactory;
import opensmt.Logic_t;
import opensmt.PTRef;
import opensmt.Pterm;
import opensmt.SRef;
import opensmt.SymRef;
import opensmt.VectorPTRef;
import opensmt.VectorSRef;
import org.sosy_lab.common.rationals.Rational;
import org.sosy_lab.java_smt.SolverContextFactory.Logics;
import org.sosy_lab.java_smt.api.ArrayFormula;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.FormulaType.ArrayFormulaType;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor;
import org.sosy_lab.java_smt.basicimpl.FormulaCreator;
import org.sosy_lab.java_smt.basicimpl.FunctionDeclarationImpl;
import org.sosy_lab.java_smt.solvers.opensmt.OpenSmtFormula.OpenSmtArrayFormula;
import org.sosy_lab.java_smt.solvers.opensmt.OpenSmtFormula.OpenSmtBooleanFormula;
import org.sosy_lab.java_smt.solvers.opensmt.OpenSmtFormula.OpenSmtIntegerFormula;
import org.sosy_lab.java_smt.solvers.opensmt.OpenSmtFormula.OpenSmtRationalFormula;

public class OpenSmtFormulaCreator extends FormulaCreator<PTRef, SRef, Logic, SymRef> {

  private OpenSmtFormulaCreator(Logic logic) {
    super(
        logic,
        logic.getSort_bool(),
        (logic instanceof ArithLogic) ? ((ArithLogic) logic).getSort_int() : null,
        (logic instanceof ArithLogic) ? ((ArithLogic) logic).getSort_real() : null,
        null,
        null);
  }

  public static OpenSmtFormulaCreator newCreator(Logics logicToUse) {
    switch (logicToUse) {
      case CORE:
      case QF_UF:
        return new OpenSmtFormulaCreator(LogicFactory.getInstance(Logic_t.QF_UF));
      case QF_LIA:
        return new OpenSmtFormulaCreator(LogicFactory.getLIAInstance());
      case QF_LRA:
        return new OpenSmtFormulaCreator(LogicFactory.getLRAInstance());
      default:
        return new OpenSmtFormulaCreator(LogicFactory.getLogicAll());
    }
  }

  @Override
  public PTRef extractInfo(Formula pT) {
    OpenSmtFormula formula = (OpenSmtFormula) pT;
    return formula.getOsmtTerm();
  }

  @Override
  public PTRef callFunctionImpl(SymRef declaration, List<PTRef> args) {
    return getEnv().insertTerm(declaration, new VectorPTRef(args));
  }

  @Override
  public SymRef declareUFImpl(String pName, SRef pReturnType, List<SRef> pArgTypes) {
    return getEnv().declareFun(pName, pReturnType, new VectorSRef(pArgTypes));
  }

  @Override
  public SRef getArrayType(SRef indexType, SRef elementType) {
    return getEnv().getArraySort(indexType, elementType);
  }

  @Override
  public SRef getBitvectorType(int bitwidth) {
    throw new UnsupportedOperationException("OpenSMT does not support bitvectors.");
  }

  @Override
  public SymRef getBooleanVarDeclarationImpl(PTRef pPTRef) {
    return getEnv().getSymRef(pPTRef);
  }

  @Override
  public SRef getFloatingPointType(FormulaType.FloatingPointType type) {
    throw new UnsupportedOperationException("OpenSMT does not support floating point operations.");
  }

  @Override
  @SuppressWarnings("MethodTypeParameterName")
  protected <TD extends Formula, TR extends Formula> FormulaType<TD> getArrayFormulaIndexType(
      ArrayFormula<TD, TR> pArray) {
    OpenSmtArrayFormula<TD, TR> array = (OpenSmtArrayFormula<TD, TR>) pArray;
    return array.getIndexType();
  }

  @Override
  @SuppressWarnings("MethodTypeParameterName")
  protected <TD extends Formula, TR extends Formula> FormulaType<TR> getArrayFormulaElementType(
      ArrayFormula<TD, TR> pArray) {
    OpenSmtArrayFormula<TD, TR> array = (OpenSmtArrayFormula<TD, TR>) pArray;
    return array.getElementType();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Formula> FormulaType<T> getFormulaType(T pFormula) {
    if (pFormula instanceof ArrayFormula<?, ?>) {
      FormulaType<T> arrayIndexType = getArrayFormulaIndexType((ArrayFormula<T, T>) pFormula);
      FormulaType<T> arrayElementType = getArrayFormulaElementType((ArrayFormula<T, T>) pFormula);
      return (FormulaType<T>) FormulaType.getArrayType(arrayIndexType, arrayElementType);
    }
    return super.getFormulaType(pFormula);
  }

  @Override
  public FormulaType<?> getFormulaType(PTRef pFormula) {
    SRef sort = getEnv().getSortRef(pFormula);
    return getFormulaTypeFromTermType(sort);
  }

  private FormulaType<?> getFormulaTypeFromTermType(SRef sort) {
    Logic logic = getEnv();
    if (logic.isSortBool(sort)) {
      return FormulaType.BooleanType;
    }
    if (logic.isArraySort(sort)) {
      VectorSRef args = getEnv().getSortDefinition(sort).getArgs();

      FormulaType<?> indexType = getFormulaTypeFromTermType(args.get(0));
      FormulaType<?> elementType = getFormulaTypeFromTermType(args.get(1));
      return FormulaType.getArrayType(indexType, elementType);
    }
    ArithLogic alogic = (ArithLogic) getEnv();
    if (alogic.isSortInt(sort)) {
      return FormulaType.IntegerType;
    }
    if (alogic.isSortReal(sort)) {
      return FormulaType.RationalType;
    }
    throw new AssertionError(String.format("Encountered unhandled Type '%s'.", sort));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Formula> T encapsulate(FormulaType<T> pType, PTRef pTerm) {
    assert pType.equals(getFormulaType(pTerm))
            || (pType.equals(FormulaType.RationalType)
                && getFormulaType(pTerm).equals(FormulaType.IntegerType))
        : String.format(
            "Cannot encapsulate formula %s of Type %s as %s", pTerm, getFormulaType(pTerm), pType);
    if (pType.isBooleanType()) {
      return (T) new OpenSmtBooleanFormula(getEnv(), pTerm);
    }
    if (pType.isIntegerType()) {
      return (T) new OpenSmtIntegerFormula(getEnv(), pTerm);
    }
    if (pType.isRationalType()) {
      return (T) new OpenSmtRationalFormula(getEnv(), pTerm);
    }
    if (pType.isArrayType()) {
      ArrayFormulaType<?, ?> arrFt = (ArrayFormulaType<?, ?>) pType;
      return (T)
          new OpenSmtArrayFormula<>(getEnv(), pTerm, arrFt.getIndexType(), arrFt.getElementType());
    }
    throw new IllegalArgumentException("Cannot create formulas of Type " + pType + " in OpenSMT");
  }

  public Formula encapsulate(PTRef pTerm) {
    return encapsulate(getFormulaType(pTerm), pTerm);
  }

  @Override
  public BooleanFormula encapsulateBoolean(PTRef pTerm) {
    assert getFormulaType(pTerm).isBooleanType()
        : String.format(
            "%s is not boolean, but %s (%s)",
            pTerm, getEnv().getSortRef(pTerm), getFormulaType(pTerm));
    return new OpenSmtBooleanFormula(getEnv(), pTerm);
  }

  @Override
  @SuppressWarnings("MethodTypeParameterName")
  protected <TI extends Formula, TE extends Formula> ArrayFormula<TI, TE> encapsulateArray(
      PTRef pTerm, FormulaType<TI> pIndexType, FormulaType<TE> pElementType) {
    assert getFormulaType(pTerm).equals(FormulaType.getArrayType(pIndexType, pElementType))
        : String.format(
            "%s is no array, but %s (%s)",
            pTerm, getEnv().getSortRef(pTerm), getFormulaType(pTerm));
    return new OpenSmtArrayFormula<>(getEnv(), pTerm, pIndexType, pElementType);
  }

  @Override
  public PTRef makeVariable(SRef type, String varName) {
    return getEnv().mkVar(type, varName);
  }

  @SuppressWarnings("unused")
  private FunctionDeclarationKind getDeclarationKind(PTRef f) {
    Logic logic = getEnv();
    /*
    AND
    IFF        If and only if.
    IMPLIES    Implication between two boolean formulas.
    ITE        If-then-else operator.
    NOT
    OR
    SELECT
    STORE 	Store and select on arrays.
    UF 	        Uninterpreted function.
    XOR 	Exclusive OR over two formulas.
    */

    if (logic.isAnd(f)) {
      return FunctionDeclarationKind.AND;
    }
    if (logic.isIff(f)) {
      return FunctionDeclarationKind.IFF;
    }
    if (logic.isImplies(f)) {
      return FunctionDeclarationKind.IMPLIES;
    }
    if (logic.isIte(f)) {
      return FunctionDeclarationKind.ITE;
    }
    if (logic.isNot(f)) {
      return FunctionDeclarationKind.NOT;
    }
    if (logic.isOr(f)) {
      return FunctionDeclarationKind.OR;
    }
    if (logic.isArraySelect(f)) {
      return FunctionDeclarationKind.SELECT;
    }
    if (logic.isArrayStore(f)) {
      return FunctionDeclarationKind.STORE;
    }
    if (logic.isUF(f)) {
      return FunctionDeclarationKind.UF;
    }
    if (logic.isXor(f)) {
      return FunctionDeclarationKind.XOR;
    }

    ArithLogic alogic = (ArithLogic) getEnv();
    /*
    ADD        Addition over integers and rationals.
    DISTINCT   Distinct operator for a set of numeric formulas.
    DIV        Division over rationals and integer division over integers.
    EQ         Equality over integers and rationals.
    GT         Greater-than over integers and rationals.
    GTE        Greater-than-or-equal over integers and rationals.
    LT 	       Less-than over integers and rationals.
    LTE        Less-than-or-equal over integers and rationals.
    MODULO     Modulo operator over integers.
    MUL        Multiplication over integers and rationals.
    SUB        Subtraction over integers and rationals.
    UMINUS     Unary minus.
    */

    if (alogic.isPlus(f)) {
      return FunctionDeclarationKind.ADD;
    }
    if (alogic.isDisequality(f)) {
      return FunctionDeclarationKind.DISTINCT;
    }
    if (alogic.isDiv(f)) {
      return FunctionDeclarationKind.DIV;
    }
    if (alogic.isEquality(f)) {
      return FunctionDeclarationKind.EQ;
    }
    if (alogic.isGeq(f)) {
      return FunctionDeclarationKind.GT;
    }
    if (alogic.isGt(f)) {
      return FunctionDeclarationKind.GTE;
    }
    if (alogic.isLeq(f)) {
      return FunctionDeclarationKind.LT;
    }
    if (alogic.isLt(f)) {
      return FunctionDeclarationKind.LTE;
    }
    if (alogic.isMod(f)) {
      return FunctionDeclarationKind.MODULO;
    }
    if (alogic.isTimes(f)) {
      return FunctionDeclarationKind.MUL;
    }
    if (alogic.isMinus(f)) {
      return FunctionDeclarationKind.SUB;
    }
    if (alogic.isNeg(f)) {
      return FunctionDeclarationKind.UMINUS;
    }

    throw new UnsupportedOperationException("Encountered unsupported declaration kind");
  }

  @Override
  public Object convertValue(PTRef value) {
    Logic logic = getEnv();
    if (logic.isTrue(value)) {
      return Boolean.TRUE;
    }
    if (logic.isFalse(value)) {
      return Boolean.FALSE;
    }
    ArithLogic alogic = (ArithLogic) getEnv();
    if (alogic.isIntConst(value)) {
      return new BigInteger(alogic.getNumConst(value));
    }
    if (alogic.isRealConst(value)) {
      Rational ratValue = Rational.ofString(alogic.getNumConst(value));
      return ratValue.isIntegral() ? ratValue.getNum() : ratValue;
    }
    throw new UnsupportedOperationException("Term `" + logic.pp(value) + "` is not a value");
  }

  @Override
  public <R> R visit(FormulaVisitor<R> visitor, Formula formula, PTRef f) {
    Logic logic = getEnv();

    if (logic.isConstant(f)) {
      return visitor.visitConstant(formula, convertValue(f));
    }

    if (logic.isVar(f)) {
      String varName = logic.getSymName(logic.getSymRef(f));
      return visitor.visitFreeVariable(formula, dequote(varName));
    }

    // FIXME: Handle abstract values for arrays?

    String varName = logic.getSymName(logic.getSymRef(f));
    
    ImmutableList.Builder<Formula> argTerms = ImmutableList.builder();
    ImmutableList.Builder<FormulaType<?>> argTypes = ImmutableList.builder();

    /* FIXME Caused crashes in ModelEvaluationTest
    VectorPTRef subterms = logic.getPterm(f).getArgs();

    for (PTRef sub : subterms) {
      argTerms.add(encapsulate(sub));
      argTypes.add(getFormulaType(sub));
    }
    */

    Pterm pterm = logic.getPterm(f);
    for (int i = 0; i < pterm.size(); i++) {
      PTRef sub = pterm.at(i);
      argTerms.add(encapsulate(sub));
      argTypes.add(getFormulaType(sub));       
    }
    
    return visitor.visitFunction(
        formula,
        argTerms.build(),
        FunctionDeclarationImpl.of(
            varName,
            getDeclarationKind(f),
            argTypes.build(),
            getFormulaType(f),
            logic.getSymRef(f)));
  }
}
