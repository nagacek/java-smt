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

package org.sosy_lab.java_smt.example.theory_solving_nqueens;

import java.io.FileWriter;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;
import java.io.IOException;

public class StatMaker {

  public static void main(String... args)
      throws InvalidConfigurationException, SolverException, InterruptedException, IOException {
    Configuration config = Configuration.defaultConfiguration();
    LogManager logger = BasicLogManager.create(config);
    ShutdownNotifier notifier = ShutdownNotifier.createDummy();

    FileWriter csv = new FileWriter(args[0] + ".csv");
    csv.write("");
    csv.close();
    csv = new FileWriter(args[0] + ".csv", true);
    csv.write("number,partial-minimal,partial,complete,classic\n");
    //csv.write("n,num,classic,num,obsv\n");
    csv.close();

    long starttime = 0;
    long endtime = 0;
    for (int i = Integer.parseInt(args[1]); i <= Integer.parseInt(args[2]); i++) {
      csv = new FileWriter(args[0] + ".csv", true);
      csv.write(i + ",");

      starttime = System.nanoTime();
      try (SolverContext context =
               SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.Z3);
           ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS,
               ProverOptions.GENERATE_ALL_SAT)) {
        NQueens myQueen = new NQueens(context, i);
        myQueen.solvePartialMin(prover);
      }
      endtime = System.nanoTime();
      csv.write( (endtime - starttime)/1000000 + ",");

      starttime = System.nanoTime();
      try (SolverContext context =
               SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.Z3);
           ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS,
               ProverOptions.GENERATE_ALL_SAT)) {
        NQueens myQueen = new NQueens(context, i);
        myQueen.solvePartial(prover);
      }
      endtime = System.nanoTime();
      csv.write( (endtime - starttime)/1000000 + ",");

      starttime = System.nanoTime();
      try (SolverContext context =
               SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.Z3);
           ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS,
               ProverOptions.GENERATE_ALL_SAT)) {
        NQueens myQueen = new NQueens(context, i);
        myQueen.solveComplete(prover);
      }
      endtime = System.nanoTime();
      csv.write( (endtime - starttime)/1000000 + ",");

      starttime = System.nanoTime();
      try (SolverContext context =
               SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.Z3);
           ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS,
               ProverOptions.GENERATE_ALL_SAT)) {
        NQueens myQueen = new NQueens(context, i);
        csv.write(myQueen.solveClassic(prover) + ",");
      }
      endtime = System.nanoTime();
      /*csv.write( (endtime - starttime)/1000000 + ",");

      starttime = System.nanoTime();
      try (SolverContext context =
               SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.Z3);
           ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS,
               ProverOptions.GENERATE_ALL_SAT)) {
        NQueens myQueen = new NQueens(context, i);
        csv.write(myQueen.solveObservantPartial(prover) + ",");
      }
      endtime = System.nanoTime();*/
      csv.write( (endtime - starttime)/1000000 + "\n");
      csv.close();
    }
  }
}

