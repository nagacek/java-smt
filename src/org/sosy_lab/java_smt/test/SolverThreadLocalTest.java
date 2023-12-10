/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sosy_lab.java_smt.test;

import static com.google.common.truth.TruthJUnit.assume;
import static org.sosy_lab.java_smt.test.ProverEnvironmentSubject.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BasicProverEnvironment;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverException;

public class SolverThreadLocalTest extends SolverBasedTest0.ParameterizedSolverBasedTest0 {
  @Test
  public void allLocalTest() throws InterruptedException, SolverException {
    requireIntegers();

    HardIntegerFormulaGenerator gen = new HardIntegerFormulaGenerator(imgr, bmgr);
    BooleanFormula formula = gen.generate(8);

    try (BasicProverEnvironment<?> prover = context.newProverEnvironment()) {
      prover.push(formula);
      assertThat(prover).isUnsatisfiable();
    }
  }

  @Test public void nonlocalContext() throws ExecutionException, InterruptedException,
                                             SolverException {
    requireIntegers();

    /* FIXME: Exception for CVC5 (related to #310?)
    io.github.cvc5.CVC5ApiException:
      Invalid call to 'cvc5::SortKind cvc5::Sort::getKind() const', expected non-null object
    */
    assume().that(solverToUse()).isNotEqualTo(Solvers.CVC5);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<SolverContext> result = executor.submit(() -> {
      Configuration config;
      try {
        config = Configuration.builder()
            .setOption("solver.solver", solverToUse().toString())
            .build();
      } catch (InvalidConfigurationException e) {
        throw new RuntimeException(e);
      }
      LogManager logger = LogManager.createTestLogManager();
      ShutdownNotifier shutdownNotifier = ShutdownManager.create().getNotifier();

      SolverContextFactory factory = new SolverContextFactory(config, logger, shutdownNotifier);
      return factory.generateContext();
    });

    executor.shutdownNow();

    SolverContext context = result.get();
    FormulaManager mgr = context.getFormulaManager();

    BooleanFormulaManager bmgr = mgr.getBooleanFormulaManager();
    IntegerFormulaManager imgr = mgr.getIntegerFormulaManager();

    HardIntegerFormulaGenerator gen = new HardIntegerFormulaGenerator(imgr, bmgr);
    BooleanFormula formula = gen.generate(8); // CVC5 throws an exception here

    try (BasicProverEnvironment<?> prover = context.newProverEnvironment()) {
      prover.push(formula);
      assertThat(prover).isUnsatisfiable();
    }
  }

  @Test
  public void nonlocalFormulaTest() throws InterruptedException, SolverException,
                                           ExecutionException {
    requireIntegers();

    /* FIXME: Exception for CVC5 (related to #310?)
    Invalid call to 'cvc5::SortKind cvc5::Sort::getKind() const', expected non-null object
    */
    assume().that(solverToUse()).isNotEqualTo(Solvers.CVC5);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<BooleanFormula> result = executor.submit(() -> {
      HardIntegerFormulaGenerator gen = new HardIntegerFormulaGenerator(imgr, bmgr);
      return gen.generate(8);
    });

    BooleanFormula formula = result.get();
    executor.shutdownNow();

    try (BasicProverEnvironment<?> prover = context.newProverEnvironment()) {
      prover.push(formula);
      assertThat(prover).isUnsatisfiable();
    }
  }

  @Test
  public void nonlocalProverTest() throws InterruptedException, ExecutionException {
    requireIntegers();

    /* FIXME: Exception for CVC5
    io.github.cvc5.CVC5ApiException:
      Given term is not associated with the node manager of this solver
     */
    assume().that(solverToUse()).isNotEqualTo(Solvers.CVC5);

    HardIntegerFormulaGenerator gen = new HardIntegerFormulaGenerator(imgr, bmgr);
    BooleanFormula formula = gen.generate(8);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Boolean> result = executor.submit(
        () -> {
          try (BasicProverEnvironment<?> prover = context.newProverEnvironment()) {
            prover.push(formula); // CVC5 throws an exception here
            return prover.isUnsat();
          }
        });

    Boolean isUnsat = result.get();
    executor.shutdownNow();

    assert isUnsat.equals(true);
  }
}