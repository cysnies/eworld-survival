package org.mozilla.javascript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.debug.DebugFrame;

public final class Interpreter extends Icode implements Evaluator {
   InterpreterData itsData;
   static final int EXCEPTION_TRY_START_SLOT = 0;
   static final int EXCEPTION_TRY_END_SLOT = 1;
   static final int EXCEPTION_HANDLER_SLOT = 2;
   static final int EXCEPTION_TYPE_SLOT = 3;
   static final int EXCEPTION_LOCAL_SLOT = 4;
   static final int EXCEPTION_SCOPE_SLOT = 5;
   static final int EXCEPTION_SLOT_SIZE = 6;

   public Interpreter() {
      super();
   }

   private static CallFrame captureFrameForGenerator(CallFrame frame) {
      frame.frozen = true;
      CallFrame result = frame.cloneFrozen();
      frame.frozen = false;
      result.parentFrame = null;
      result.frameIndex = 0;
      return result;
   }

   public Object compile(CompilerEnvirons compilerEnv, ScriptNode tree, String encodedSource, boolean returnFunction) {
      CodeGenerator cgen = new CodeGenerator();
      this.itsData = cgen.compile(compilerEnv, tree, encodedSource, returnFunction);
      return this.itsData;
   }

   public Script createScriptObject(Object bytecode, Object staticSecurityDomain) {
      if (bytecode != this.itsData) {
         Kit.codeBug();
      }

      return InterpretedFunction.createScript(this.itsData, staticSecurityDomain);
   }

   public void setEvalScriptFlag(Script script) {
      ((InterpretedFunction)script).idata.evalScriptFlag = true;
   }

   public Function createFunctionObject(Context cx, Scriptable scope, Object bytecode, Object staticSecurityDomain) {
      if (bytecode != this.itsData) {
         Kit.codeBug();
      }

      return InterpretedFunction.createFunction(cx, scope, this.itsData, staticSecurityDomain);
   }

   private static int getShort(byte[] iCode, int pc) {
      return iCode[pc] << 8 | iCode[pc + 1] & 255;
   }

   private static int getIndex(byte[] iCode, int pc) {
      return (iCode[pc] & 255) << 8 | iCode[pc + 1] & 255;
   }

   private static int getInt(byte[] iCode, int pc) {
      return iCode[pc] << 24 | (iCode[pc + 1] & 255) << 16 | (iCode[pc + 2] & 255) << 8 | iCode[pc + 3] & 255;
   }

   private static int getExceptionHandler(CallFrame frame, boolean onlyFinally) {
      int[] exceptionTable = frame.idata.itsExceptionTable;
      if (exceptionTable == null) {
         return -1;
      } else {
         int pc = frame.pc - 1;
         int best = -1;
         int bestStart = 0;
         int bestEnd = 0;

         for(int i = 0; i != exceptionTable.length; i += 6) {
            int start = exceptionTable[i + 0];
            int end = exceptionTable[i + 1];
            if (start <= pc && pc < end && (!onlyFinally || exceptionTable[i + 3] == 1)) {
               if (best >= 0) {
                  if (bestEnd < end) {
                     continue;
                  }

                  if (bestStart > start) {
                     Kit.codeBug();
                  }

                  if (bestEnd == end) {
                     Kit.codeBug();
                  }
               }

               best = i;
               bestStart = start;
               bestEnd = end;
            }
         }

         return best;
      }
   }

   static void dumpICode(InterpreterData idata) {
   }

   private static int bytecodeSpan(int bytecode) {
      switch (bytecode) {
         case -63:
         case -62:
         case 50:
         case 72:
            return 3;
         case -61:
         case -49:
         case -48:
            return 2;
         case -54:
         case -23:
         case -6:
         case 5:
         case 6:
         case 7:
            return 3;
         case -47:
            return 5;
         case -46:
            return 3;
         case -45:
            return 2;
         case -40:
            return 5;
         case -39:
            return 3;
         case -38:
            return 2;
         case -28:
            return 5;
         case -27:
            return 3;
         case -26:
            return 3;
         case -21:
            return 5;
         case -11:
         case -10:
         case -9:
         case -8:
         case -7:
            return 2;
         case 57:
            return 2;
         default:
            if (!validBytecode(bytecode)) {
               throw Kit.codeBug();
            } else {
               return 1;
            }
      }
   }

   static int[] getLineNumbers(InterpreterData data) {
      UintMap presentLines = new UintMap();
      byte[] iCode = data.itsICode;
      int iCodeLength = iCode.length;

      int span;
      for(int pc = 0; pc != iCodeLength; pc += span) {
         int bytecode = iCode[pc];
         span = bytecodeSpan(bytecode);
         if (bytecode == -26) {
            if (span != 3) {
               Kit.codeBug();
            }

            int line = getIndex(iCode, pc + 1);
            presentLines.put(line, 0);
         }
      }

      return presentLines.getKeys();
   }

   public void captureStackInfo(RhinoException ex) {
      Context cx = Context.getCurrentContext();
      if (cx != null && cx.lastInterpreterFrame != null) {
         CallFrame[] array;
         if (cx.previousInterpreterInvocations != null && cx.previousInterpreterInvocations.size() != 0) {
            int previousCount = cx.previousInterpreterInvocations.size();
            if (cx.previousInterpreterInvocations.peek() == cx.lastInterpreterFrame) {
               --previousCount;
            }

            array = new CallFrame[previousCount + 1];
            cx.previousInterpreterInvocations.toArray(array);
         } else {
            array = new CallFrame[1];
         }

         array[array.length - 1] = (CallFrame)cx.lastInterpreterFrame;
         int interpreterFrameCount = 0;

         for(int i = 0; i != array.length; ++i) {
            interpreterFrameCount += 1 + array[i].frameIndex;
         }

         int[] linePC = new int[interpreterFrameCount];
         int linePCIndex = interpreterFrameCount;
         int i = array.length;

         while(i != 0) {
            --i;

            for(CallFrame frame = array[i]; frame != null; frame = frame.parentFrame) {
               --linePCIndex;
               linePC[linePCIndex] = frame.pcSourceLineStart;
            }
         }

         if (linePCIndex != 0) {
            Kit.codeBug();
         }

         ex.interpreterStackInfo = array;
         ex.interpreterLineData = linePC;
      } else {
         ex.interpreterStackInfo = null;
         ex.interpreterLineData = null;
      }
   }

   public String getSourcePositionFromStack(Context cx, int[] linep) {
      CallFrame frame = (CallFrame)cx.lastInterpreterFrame;
      InterpreterData idata = frame.idata;
      if (frame.pcSourceLineStart >= 0) {
         linep[0] = getIndex(idata.itsICode, frame.pcSourceLineStart);
      } else {
         linep[0] = 0;
      }

      return idata.itsSourceFile;
   }

   public String getPatchedStack(RhinoException ex, String nativeStackTrace) {
      String tag = "org.mozilla.javascript.Interpreter.interpretLoop";
      StringBuffer sb = new StringBuffer(nativeStackTrace.length() + 1000);
      String lineSeparator = SecurityUtilities.getSystemProperty("line.separator");
      CallFrame[] array = (CallFrame[])ex.interpreterStackInfo;
      int[] linePC = ex.interpreterLineData;
      int arrayIndex = array.length;
      int linePCIndex = linePC.length;
      int offset = 0;

      while(arrayIndex != 0) {
         --arrayIndex;
         int pos = nativeStackTrace.indexOf(tag, offset);
         if (pos < 0) {
            break;
         }

         for(pos += tag.length(); pos != nativeStackTrace.length(); ++pos) {
            char c = nativeStackTrace.charAt(pos);
            if (c == '\n' || c == '\r') {
               break;
            }
         }

         sb.append(nativeStackTrace.substring(offset, pos));
         offset = pos;

         for(CallFrame frame = array[arrayIndex]; frame != null; frame = frame.parentFrame) {
            if (linePCIndex == 0) {
               Kit.codeBug();
            }

            --linePCIndex;
            InterpreterData idata = frame.idata;
            sb.append(lineSeparator);
            sb.append("\tat script");
            if (idata.itsName != null && idata.itsName.length() != 0) {
               sb.append('.');
               sb.append(idata.itsName);
            }

            sb.append('(');
            sb.append(idata.itsSourceFile);
            int pc = linePC[linePCIndex];
            if (pc >= 0) {
               sb.append(':');
               sb.append(getIndex(idata.itsICode, pc));
            }

            sb.append(')');
         }
      }

      sb.append(nativeStackTrace.substring(offset));
      return sb.toString();
   }

   public List getScriptStack(RhinoException ex) {
      ScriptStackElement[][] stack = this.getScriptStackElements(ex);
      List<String> list = new ArrayList(stack.length);
      String lineSeparator = SecurityUtilities.getSystemProperty("line.separator");

      for(ScriptStackElement[] group : stack) {
         StringBuilder sb = new StringBuilder();

         for(ScriptStackElement elem : group) {
            elem.renderJavaStyle(sb);
            sb.append(lineSeparator);
         }

         list.add(sb.toString());
      }

      return list;
   }

   public ScriptStackElement[][] getScriptStackElements(RhinoException ex) {
      if (ex.interpreterStackInfo == null) {
         return (ScriptStackElement[][])null;
      } else {
         List<ScriptStackElement[]> list = new ArrayList();
         CallFrame[] array = (CallFrame[])ex.interpreterStackInfo;
         int[] linePC = ex.interpreterLineData;
         int arrayIndex = array.length;
         int linePCIndex = linePC.length;

         while(arrayIndex != 0) {
            --arrayIndex;
            CallFrame frame = array[arrayIndex];
            List<ScriptStackElement> group = new ArrayList();

            while(frame != null) {
               if (linePCIndex == 0) {
                  Kit.codeBug();
               }

               --linePCIndex;
               InterpreterData idata = frame.idata;
               String fileName = idata.itsSourceFile;
               String functionName = null;
               int lineNumber = -1;
               int pc = linePC[linePCIndex];
               if (pc >= 0) {
                  lineNumber = getIndex(idata.itsICode, pc);
               }

               if (idata.itsName != null && idata.itsName.length() != 0) {
                  functionName = idata.itsName;
               }

               frame = frame.parentFrame;
               group.add(new ScriptStackElement(fileName, functionName, lineNumber));
            }

            list.add(group.toArray(new ScriptStackElement[group.size()]));
         }

         return (ScriptStackElement[][])list.toArray(new ScriptStackElement[list.size()][]);
      }
   }

   static String getEncodedSource(InterpreterData idata) {
      return idata.encodedSource == null ? null : idata.encodedSource.substring(idata.encodedSourceStart, idata.encodedSourceEnd);
   }

   private static void initFunction(Context cx, Scriptable scope, InterpretedFunction parent, int index) {
      InterpretedFunction fn = InterpretedFunction.createFunction(cx, scope, parent, index);
      ScriptRuntime.initFunction(cx, scope, fn, fn.idata.itsFunctionType, parent.idata.evalScriptFlag);
   }

   static Object interpret(InterpretedFunction ifun, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!ScriptRuntime.hasTopCall(cx)) {
         Kit.codeBug();
      }

      if (cx.interpreterSecurityDomain != ifun.securityDomain) {
         Object savedDomain = cx.interpreterSecurityDomain;
         cx.interpreterSecurityDomain = ifun.securityDomain;

         Object var6;
         try {
            var6 = ifun.securityController.callWithDomain(ifun.securityDomain, cx, ifun, scope, thisObj, args);
         } finally {
            cx.interpreterSecurityDomain = savedDomain;
         }

         return var6;
      } else {
         CallFrame frame = new CallFrame();
         initFrame(cx, scope, thisObj, args, (double[])null, 0, args.length, ifun, (CallFrame)null, frame);
         frame.isContinuationsTopFrame = cx.isContinuationsTopCall;
         cx.isContinuationsTopCall = false;
         return interpretLoop(cx, frame, (Object)null);
      }
   }

   public static Object resumeGenerator(Context cx, Scriptable scope, int operation, Object savedState, Object value) {
      CallFrame frame = (CallFrame)savedState;
      GeneratorState generatorState = new GeneratorState(operation, value);
      if (operation == 2) {
         try {
            return interpretLoop(cx, frame, generatorState);
         } catch (RuntimeException e) {
            if (e != value) {
               throw e;
            } else {
               return Undefined.instance;
            }
         }
      } else {
         Object result = interpretLoop(cx, frame, generatorState);
         if (generatorState.returnedException != null) {
            throw generatorState.returnedException;
         } else {
            return result;
         }
      }
   }

   public static Object restartContinuation(NativeContinuation c, Context cx, Scriptable scope, Object[] args) {
      if (!ScriptRuntime.hasTopCall(cx)) {
         return ScriptRuntime.doTopCall(c, cx, scope, (Scriptable)null, args);
      } else {
         Object arg;
         if (args.length == 0) {
            arg = Undefined.instance;
         } else {
            arg = args[0];
         }

         CallFrame capturedFrame = (CallFrame)c.getImplementation();
         if (capturedFrame == null) {
            return arg;
         } else {
            ContinuationJump cjump = new ContinuationJump(c, (CallFrame)null);
            cjump.result = arg;
            return interpretLoop(cx, (CallFrame)null, cjump);
         }
      }
   }

   private static Object interpretLoop(Context cx, CallFrame frame, Object throwable) {
      Object DBL_MRK = UniqueTag.DOUBLE_MARK;
      Object undefined = Undefined.instance;
      boolean instructionCounting = cx.instructionThreshold != 0;
      int INVOCATION_COST = 100;
      int EXCEPTION_COST = 100;
      String stringReg = null;
      int indexReg = -1;
      if (cx.lastInterpreterFrame != null) {
         if (cx.previousInterpreterInvocations == null) {
            cx.previousInterpreterInvocations = new ObjArray();
         }

         cx.previousInterpreterInvocations.push(cx.lastInterpreterFrame);
      }

      GeneratorState generatorState = null;
      if (throwable != null) {
         if (throwable instanceof GeneratorState) {
            generatorState = (GeneratorState)throwable;
            enterFrame(cx, frame, ScriptRuntime.emptyArgs, true);
            throwable = null;
         } else if (!(throwable instanceof ContinuationJump)) {
            Kit.codeBug();
         }
      }

      Object interpreterResult = null;
      double interpreterResultDbl = (double)0.0F;

      label1158:
      while(true) {
         label1339: {
            try {
               if (throwable != null) {
                  frame = processThrowable(cx, throwable, frame, indexReg, instructionCounting);
                  throwable = frame.throwable;
                  frame.throwable = null;
               } else if (generatorState == null && frame.frozen) {
                  Kit.codeBug();
               }

               Object[] stack = frame.stack;
               double[] sDbl = frame.sDbl;
               Object[] vars = frame.varSource.stack;
               double[] varDbls = frame.varSource.sDbl;
               int[] varAttributes = frame.varSource.stackAttributes;
               byte[] iCode = frame.idata.itsICode;
               String[] strings = frame.idata.itsStringTable;
               int stackTop = frame.savedStackTop;
               cx.lastInterpreterFrame = frame;

               label1141:
               while(true) {
                  label1079:
                  while(true) {
                     label1077:
                     while(true) {
                        label1075:
                        while(true) {
                           label998:
                           while(true) {
                              int op = iCode[frame.pc++];
                              switch (op) {
                                 case -64:
                                    if (frame.debuggerFrame != null) {
                                       frame.debuggerFrame.onDebuggerStatement(cx);
                                    }
                                    continue;
                                 case -63:
                                    frame.frozen = true;
                                    int sourceLine = getIndex(iCode, frame.pc);
                                    generatorState.returnedException = new JavaScriptException(NativeIterator.getStopIterationObject(frame.scope), frame.idata.itsSourceFile, sourceLine);
                                    break;
                                 case -62:
                                    if (!frame.frozen) {
                                       --frame.pc;
                                       CallFrame generatorFrame = captureFrameForGenerator(frame);
                                       generatorFrame.frozen = true;
                                       NativeGenerator generator = new NativeGenerator(frame.scope, generatorFrame.fnOrScript, generatorFrame);
                                       frame.result = generator;
                                       break;
                                    }
                                 case 72:
                                    if (!frame.frozen) {
                                       return freezeGenerator(cx, frame, stackTop, generatorState);
                                    }

                                    Object obj = thawGenerator(frame, stackTop, generatorState, op);
                                    if (obj != Scriptable.NOT_FOUND) {
                                       throwable = obj;
                                       break label1141;
                                    }
                                    continue;
                                 case -61:
                                    indexReg = iCode[frame.pc++];
                                    break label1079;
                                 case -60:
                                 case 1:
                                 case 81:
                                 case 82:
                                 case 83:
                                 case 84:
                                 case 85:
                                 case 86:
                                 case 87:
                                 case 88:
                                 case 89:
                                 case 90:
                                 case 91:
                                 case 92:
                                 case 93:
                                 case 94:
                                 case 95:
                                 case 96:
                                 case 97:
                                 case 98:
                                 case 99:
                                 case 100:
                                 case 101:
                                 case 102:
                                 case 103:
                                 case 104:
                                 case 105:
                                 case 106:
                                 case 107:
                                 case 108:
                                 case 109:
                                 case 110:
                                 case 111:
                                 case 112:
                                 case 113:
                                 case 114:
                                 case 115:
                                 case 116:
                                 case 117:
                                 case 118:
                                 case 119:
                                 case 120:
                                 case 121:
                                 case 122:
                                 case 123:
                                 case 124:
                                 case 125:
                                 case 126:
                                 case 127:
                                 case 128:
                                 case 129:
                                 case 130:
                                 case 131:
                                 case 132:
                                 case 133:
                                 case 134:
                                 case 135:
                                 case 136:
                                 case 137:
                                 case 138:
                                 case 139:
                                 case 140:
                                 case 141:
                                 case 142:
                                 case 143:
                                 case 144:
                                 case 145:
                                 case 146:
                                 case 147:
                                 case 148:
                                 case 149:
                                 case 150:
                                 case 151:
                                 case 152:
                                 case 153:
                                 case 154:
                                 case 155:
                                 default:
                                    dumpICode(frame.idata);
                                    throw new RuntimeException("Unknown icode : " + op + " @ pc : " + (frame.pc - 1));
                                 case -59:
                                    Object rhs = stack[stackTop];
                                    if (rhs == DBL_MRK) {
                                       rhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    Scriptable lhs = (Scriptable)stack[stackTop];
                                    stack[stackTop] = ScriptRuntime.setConst(lhs, rhs, cx, stringReg);
                                    continue;
                                 case -58:
                                    Object value = stack[stackTop];
                                    --stackTop;
                                    int i = (int)sDbl[stackTop];
                                    ((Object[])((Object[])stack[stackTop]))[i] = value;
                                    ((int[])((int[])stack[stackTop - 1]))[i] = 1;
                                    sDbl[stackTop] = (double)(i + 1);
                                    continue;
                                 case -57:
                                    Object value = stack[stackTop];
                                    --stackTop;
                                    int i = (int)sDbl[stackTop];
                                    ((Object[])((Object[])stack[stackTop]))[i] = value;
                                    ((int[])((int[])stack[stackTop - 1]))[i] = -1;
                                    sDbl[stackTop] = (double)(i + 1);
                                    continue;
                                 case -56:
                                    indexReg += frame.localShift;
                                    stack[indexReg] = null;
                                    continue;
                                 case -55:
                                 case 38:
                                 case 70:
                                    if (instructionCounting) {
                                       cx.instructionCount += 100;
                                    }

                                    stackTop -= 1 + indexReg;
                                    Callable fun = (Callable)stack[stackTop];
                                    Scriptable funThisObj = (Scriptable)stack[stackTop + 1];
                                    if (op == 70) {
                                       Object[] outArgs = getArgsArray(stack, sDbl, stackTop + 2, indexReg);
                                       stack[stackTop] = ScriptRuntime.callRef(fun, funThisObj, outArgs, cx);
                                    } else {
                                       Scriptable calleeScope = frame.scope;
                                       if (frame.useActivation) {
                                          calleeScope = ScriptableObject.getTopLevelScope(frame.scope);
                                       }

                                       if (fun instanceof InterpretedFunction) {
                                          InterpretedFunction ifun = (InterpretedFunction)fun;
                                          if (frame.fnOrScript.securityDomain == ifun.securityDomain) {
                                             CallFrame callParentFrame = frame;
                                             CallFrame calleeFrame = new CallFrame();
                                             if (op == -55) {
                                                callParentFrame = frame.parentFrame;
                                                exitFrame(cx, frame, (Object)null);
                                             }

                                             initFrame(cx, calleeScope, funThisObj, stack, sDbl, stackTop + 2, indexReg, ifun, callParentFrame, calleeFrame);
                                             if (op != -55) {
                                                frame.savedStackTop = stackTop;
                                                frame.savedCallOp = op;
                                             }

                                             frame = calleeFrame;
                                             continue label1158;
                                          }
                                       }

                                       if (fun instanceof NativeContinuation) {
                                          ContinuationJump cjump = new ContinuationJump((NativeContinuation)fun, frame);
                                          if (indexReg == 0) {
                                             cjump.result = undefined;
                                          } else {
                                             cjump.result = stack[stackTop + 2];
                                             cjump.resultDbl = sDbl[stackTop + 2];
                                          }

                                          throwable = cjump;
                                          break label1141;
                                       }

                                       if (fun instanceof IdFunctionObject) {
                                          IdFunctionObject ifun = (IdFunctionObject)fun;
                                          if (NativeContinuation.isContinuationConstructor(ifun)) {
                                             frame.stack[stackTop] = captureContinuation(cx, frame.parentFrame, false);
                                             continue;
                                          }

                                          if (BaseFunction.isApplyOrCall(ifun)) {
                                             Callable applyCallable = ScriptRuntime.getCallable(funThisObj);
                                             if (applyCallable instanceof InterpretedFunction) {
                                                InterpretedFunction iApplyCallable = (InterpretedFunction)applyCallable;
                                                if (frame.fnOrScript.securityDomain == iApplyCallable.securityDomain) {
                                                   frame = initFrameForApplyOrCall(cx, frame, indexReg, stack, sDbl, stackTop, op, calleeScope, ifun, iApplyCallable);
                                                   continue label1158;
                                                }
                                             }
                                          }
                                       }

                                       if (fun instanceof ScriptRuntime.NoSuchMethodShim) {
                                          ScriptRuntime.NoSuchMethodShim noSuchMethodShim = (ScriptRuntime.NoSuchMethodShim)fun;
                                          Callable noSuchMethodMethod = noSuchMethodShim.noSuchMethodMethod;
                                          if (noSuchMethodMethod instanceof InterpretedFunction) {
                                             InterpretedFunction ifun = (InterpretedFunction)noSuchMethodMethod;
                                             if (frame.fnOrScript.securityDomain == ifun.securityDomain) {
                                                frame = initFrameForNoSuchMethod(cx, frame, indexReg, stack, sDbl, stackTop, op, funThisObj, calleeScope, noSuchMethodShim, ifun);
                                                continue label1158;
                                             }
                                          }
                                       }

                                       cx.lastInterpreterFrame = frame;
                                       frame.savedCallOp = op;
                                       frame.savedStackTop = stackTop;
                                       stack[stackTop] = fun.call(cx, calleeScope, funThisObj, getArgsArray(stack, sDbl, stackTop + 2, indexReg));
                                    }
                                    continue;
                                 case -54:
                                    boolean valBln = stack_boolean(frame, stackTop);
                                    Object x = ScriptRuntime.updateDotQuery(valBln, frame.scope);
                                    if (x != null) {
                                       stack[stackTop] = x;
                                       frame.scope = ScriptRuntime.leaveDotQuery(frame.scope);
                                       frame.pc += 2;
                                       continue;
                                    } else {
                                       --stackTop;
                                       break label998;
                                    }
                                 case -53:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    frame.scope = ScriptRuntime.enterDotQuery(lhs, frame.scope);
                                    continue;
                                 case -52:
                                    ++stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)1.0F;
                                    continue;
                                 case -51:
                                    ++stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)0.0F;
                                    continue;
                                 case -50:
                                    ++stackTop;
                                    stack[stackTop] = undefined;
                                    continue;
                                 case -49:
                                    indexReg = iCode[frame.pc++];
                                    break label1077;
                                 case -48:
                                    indexReg = iCode[frame.pc++];
                                    break label1075;
                                 case -47:
                                    stringReg = strings[getInt(iCode, frame.pc)];
                                    frame.pc += 4;
                                    continue;
                                 case -46:
                                    stringReg = strings[getIndex(iCode, frame.pc)];
                                    frame.pc += 2;
                                    continue;
                                 case -45:
                                    stringReg = strings[255 & iCode[frame.pc]];
                                    ++frame.pc;
                                    continue;
                                 case -44:
                                    stringReg = strings[3];
                                    continue;
                                 case -43:
                                    stringReg = strings[2];
                                    continue;
                                 case -42:
                                    stringReg = strings[1];
                                    continue;
                                 case -41:
                                    stringReg = strings[0];
                                    continue;
                                 case -40:
                                    indexReg = getInt(iCode, frame.pc);
                                    frame.pc += 4;
                                    continue;
                                 case -39:
                                    indexReg = getIndex(iCode, frame.pc);
                                    frame.pc += 2;
                                    continue;
                                 case -38:
                                    indexReg = 255 & iCode[frame.pc];
                                    ++frame.pc;
                                    continue;
                                 case -37:
                                    indexReg = 5;
                                    continue;
                                 case -36:
                                    indexReg = 4;
                                    continue;
                                 case -35:
                                    indexReg = 3;
                                    continue;
                                 case -34:
                                    indexReg = 2;
                                    continue;
                                 case -33:
                                    indexReg = 1;
                                    continue;
                                 case -32:
                                    indexReg = 0;
                                    continue;
                                 case -31:
                                 case 65:
                                 case 66:
                                    Object[] data = stack[stackTop];
                                    --stackTop;
                                    int[] getterSetters = (int[])stack[stackTop];
                                    Object val;
                                    if (op == 66) {
                                       Object[] ids = frame.idata.literalIds[indexReg];
                                       val = ScriptRuntime.newObjectLiteral(ids, data, getterSetters, cx, frame.scope);
                                    } else {
                                       int[] skipIndexces = null;
                                       if (op == -31) {
                                          skipIndexces = (int[])frame.idata.literalIds[indexReg];
                                       }

                                       val = ScriptRuntime.newArrayLiteral(data, skipIndexces, cx, frame.scope);
                                    }

                                    stack[stackTop] = val;
                                    continue;
                                 case -30:
                                    Object value = stack[stackTop];
                                    if (value == DBL_MRK) {
                                       value = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    int i = (int)sDbl[stackTop];
                                    ((Object[])((Object[])stack[stackTop]))[i] = value;
                                    sDbl[stackTop] = (double)(i + 1);
                                    continue;
                                 case -29:
                                    ++stackTop;
                                    stack[stackTop] = new int[indexReg];
                                    ++stackTop;
                                    stack[stackTop] = new Object[indexReg];
                                    sDbl[stackTop] = (double)0.0F;
                                    continue;
                                 case -28:
                                    ++stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)getInt(iCode, frame.pc);
                                    frame.pc += 4;
                                    continue;
                                 case -27:
                                    ++stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)getShort(iCode, frame.pc);
                                    frame.pc += 2;
                                    continue;
                                 case -26:
                                    frame.pcSourceLineStart = frame.pc;
                                    if (frame.debuggerFrame != null) {
                                       int line = getIndex(iCode, frame.pc);
                                       frame.debuggerFrame.onLineChange(cx, line);
                                    }

                                    frame.pc += 2;
                                    continue;
                                 case -25:
                                    if (instructionCounting) {
                                       addInstructionCount(cx, frame, 0);
                                    }

                                    indexReg += frame.localShift;
                                    Object value = stack[indexReg];
                                    if (value != DBL_MRK) {
                                       throwable = value;
                                       break label1141;
                                    }

                                    frame.pc = (int)sDbl[indexReg];
                                    if (instructionCounting) {
                                       frame.pcPrevBranch = frame.pc;
                                    }
                                    continue;
                                 case -24:
                                    if (stackTop == frame.emptyStackTop + 1) {
                                       indexReg += frame.localShift;
                                       stack[indexReg] = stack[stackTop];
                                       sDbl[indexReg] = sDbl[stackTop];
                                       --stackTop;
                                    } else if (stackTop != frame.emptyStackTop) {
                                       Kit.codeBug();
                                    }
                                    continue;
                                 case -23:
                                    ++stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)(frame.pc + 2);
                                    break label998;
                                 case -22:
                                    frame.result = undefined;
                                    break;
                                 case -21:
                                    if (instructionCounting) {
                                       cx.instructionCount += 100;
                                    }

                                    stackTop = doCallSpecial(cx, frame, stack, sDbl, stackTop, iCode, indexReg);
                                    continue;
                                 case -20:
                                    initFunction(cx, frame.scope, frame.fnOrScript, indexReg);
                                    continue;
                                 case -19:
                                    ++stackTop;
                                    stack[stackTop] = InterpretedFunction.createFunction(cx, frame.scope, frame.fnOrScript, indexReg);
                                    continue;
                                 case -18:
                                    Object value = stack[stackTop];
                                    if (value == DBL_MRK) {
                                       value = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.getValueFunctionAndThis(value, cx);
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.lastStoredScriptable(cx);
                                    continue;
                                 case -17:
                                    Object obj = stack[stackTop - 1];
                                    if (obj == DBL_MRK) {
                                       obj = ScriptRuntime.wrapNumber(sDbl[stackTop - 1]);
                                    }

                                    Object id = stack[stackTop];
                                    if (id == DBL_MRK) {
                                       id = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop - 1] = ScriptRuntime.getElemFunctionAndThis(obj, id, cx);
                                    stack[stackTop] = ScriptRuntime.lastStoredScriptable(cx);
                                    continue;
                                 case -16:
                                    Object obj = stack[stackTop];
                                    if (obj == DBL_MRK) {
                                       obj = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.getPropFunctionAndThis(obj, stringReg, cx, frame.scope);
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.lastStoredScriptable(cx);
                                    continue;
                                 case -15:
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.getNameFunctionAndThis(stringReg, cx, frame.scope);
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.lastStoredScriptable(cx);
                                    continue;
                                 case -14:
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.typeofName(frame.scope, stringReg);
                                    continue;
                                 case -13:
                                    indexReg += frame.localShift;
                                    stack[indexReg] = frame.scope;
                                    continue;
                                 case -12:
                                    indexReg += frame.localShift;
                                    frame.scope = (Scriptable)stack[indexReg];
                                    continue;
                                 case -11:
                                    Ref ref = (Ref)stack[stackTop];
                                    stack[stackTop] = ScriptRuntime.refIncrDecr(ref, cx, iCode[frame.pc]);
                                    ++frame.pc;
                                    continue;
                                 case -10:
                                    stackTop = doElemIncDec(cx, frame, iCode, stack, sDbl, stackTop);
                                    continue;
                                 case -9:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.propIncrDecr(lhs, stringReg, cx, iCode[frame.pc]);
                                    ++frame.pc;
                                    continue;
                                 case -8:
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.nameIncrDecr(frame.scope, stringReg, cx, iCode[frame.pc]);
                                    ++frame.pc;
                                    continue;
                                 case -7:
                                    stackTop = doVarIncDec(cx, frame, stack, sDbl, stackTop, vars, varDbls, indexReg);
                                    continue;
                                 case -6:
                                    if (!stack_boolean(frame, stackTop--)) {
                                       frame.pc += 2;
                                       continue;
                                    } else {
                                       stack[stackTop--] = null;
                                       break label998;
                                    }
                                 case -5:
                                    frame.result = stack[stackTop];
                                    frame.resultDbl = sDbl[stackTop];
                                    stack[stackTop] = null;
                                    --stackTop;
                                    continue;
                                 case -4:
                                    stack[stackTop] = null;
                                    --stackTop;
                                    continue;
                                 case -3:
                                    Object o = stack[stackTop];
                                    stack[stackTop] = stack[stackTop - 1];
                                    stack[stackTop - 1] = o;
                                    double d = sDbl[stackTop];
                                    sDbl[stackTop] = sDbl[stackTop - 1];
                                    sDbl[stackTop - 1] = d;
                                    continue;
                                 case -2:
                                    stack[stackTop + 1] = stack[stackTop - 1];
                                    sDbl[stackTop + 1] = sDbl[stackTop - 1];
                                    stack[stackTop + 2] = stack[stackTop];
                                    sDbl[stackTop + 2] = sDbl[stackTop];
                                    stackTop += 2;
                                    continue;
                                 case -1:
                                    stack[stackTop + 1] = stack[stackTop];
                                    sDbl[stackTop + 1] = sDbl[stackTop];
                                    ++stackTop;
                                    continue;
                                 case 0:
                                 case 31:
                                    stackTop = doDelName(cx, op, stack, sDbl, stackTop);
                                    continue;
                                 case 2:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    frame.scope = ScriptRuntime.enterWith(lhs, cx, frame.scope);
                                    continue;
                                 case 3:
                                    frame.scope = ScriptRuntime.leaveWith(frame.scope);
                                    continue;
                                 case 4:
                                    frame.result = stack[stackTop];
                                    frame.resultDbl = sDbl[stackTop];
                                    --stackTop;
                                    break;
                                 case 5:
                                    break label998;
                                 case 6:
                                    if (!stack_boolean(frame, stackTop--)) {
                                       frame.pc += 2;
                                       continue;
                                    }
                                    break label998;
                                 case 7:
                                    if (stack_boolean(frame, stackTop--)) {
                                       frame.pc += 2;
                                       continue;
                                    }
                                    break label998;
                                 case 8:
                                 case 73:
                                    Object rhs = stack[stackTop];
                                    if (rhs == DBL_MRK) {
                                       rhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    Scriptable lhs = (Scriptable)stack[stackTop];
                                    stack[stackTop] = op == 8 ? ScriptRuntime.setName(lhs, rhs, cx, frame.scope, stringReg) : ScriptRuntime.strictSetName(lhs, rhs, cx, frame.scope, stringReg);
                                    continue;
                                 case 9:
                                 case 10:
                                 case 11:
                                 case 18:
                                 case 19:
                                    stackTop = doBitOp(frame, op, stack, sDbl, stackTop);
                                    continue;
                                 case 12:
                                 case 13:
                                    --stackTop;
                                    boolean valBln = doEquals(stack, sDbl, stackTop);
                                    valBln ^= op == 13;
                                    stack[stackTop] = ScriptRuntime.wrapBoolean(valBln);
                                    continue;
                                 case 14:
                                 case 15:
                                 case 16:
                                 case 17:
                                    stackTop = doCompare(frame, op, stack, sDbl, stackTop);
                                    continue;
                                 case 20:
                                    double lDbl = stack_double(frame, stackTop - 1);
                                    int rIntValue = stack_int32(frame, stackTop) & 31;
                                    --stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)(ScriptRuntime.toUint32(lDbl) >>> rIntValue);
                                    continue;
                                 case 21:
                                    --stackTop;
                                    doAdd(stack, sDbl, stackTop, cx);
                                    continue;
                                 case 22:
                                 case 23:
                                 case 24:
                                 case 25:
                                    stackTop = doArithmetic(frame, op, stack, sDbl, stackTop);
                                    continue;
                                 case 26:
                                    stack[stackTop] = ScriptRuntime.wrapBoolean(!stack_boolean(frame, stackTop));
                                    continue;
                                 case 27:
                                    int rIntValue = stack_int32(frame, stackTop);
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = (double)(~rIntValue);
                                    continue;
                                 case 28:
                                 case 29:
                                    double rDbl = stack_double(frame, stackTop);
                                    stack[stackTop] = DBL_MRK;
                                    if (op == 29) {
                                       rDbl = -rDbl;
                                    }

                                    sDbl[stackTop] = rDbl;
                                    continue;
                                 case 30:
                                    if (instructionCounting) {
                                       cx.instructionCount += 100;
                                    }

                                    stackTop -= indexReg;
                                    Object lhs = stack[stackTop];
                                    if (lhs instanceof InterpretedFunction) {
                                       InterpretedFunction f = (InterpretedFunction)lhs;
                                       if (frame.fnOrScript.securityDomain == f.securityDomain) {
                                          Scriptable newInstance = f.createObject(cx, frame.scope);
                                          CallFrame calleeFrame = new CallFrame();
                                          initFrame(cx, frame.scope, newInstance, stack, sDbl, stackTop + 1, indexReg, f, frame, calleeFrame);
                                          stack[stackTop] = newInstance;
                                          frame.savedStackTop = stackTop;
                                          frame.savedCallOp = op;
                                          frame = calleeFrame;
                                          continue label1158;
                                       }
                                    }

                                    if (!(lhs instanceof Function)) {
                                       if (lhs == DBL_MRK) {
                                          lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                       }

                                       throw ScriptRuntime.notFunctionError(lhs);
                                    }

                                    Function fun = (Function)lhs;
                                    if (fun instanceof IdFunctionObject) {
                                       IdFunctionObject ifun = (IdFunctionObject)fun;
                                       if (NativeContinuation.isContinuationConstructor(ifun)) {
                                          frame.stack[stackTop] = captureContinuation(cx, frame.parentFrame, false);
                                          continue;
                                       }
                                    }

                                    Object[] outArgs = getArgsArray(stack, sDbl, stackTop + 1, indexReg);
                                    stack[stackTop] = fun.construct(cx, frame.scope, outArgs);
                                    continue;
                                 case 32:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.typeof(lhs);
                                    continue;
                                 case 33:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.getObjectProp(lhs, stringReg, cx, frame.scope);
                                    continue;
                                 case 34:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.getObjectPropNoWarn(lhs, stringReg, cx);
                                    continue;
                                 case 35:
                                    Object rhs = stack[stackTop];
                                    if (rhs == DBL_MRK) {
                                       rhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.setObjectProp(lhs, stringReg, rhs, cx);
                                    continue;
                                 case 36:
                                    stackTop = doGetElem(cx, frame, stack, sDbl, stackTop);
                                    continue;
                                 case 37:
                                    stackTop = doSetElem(cx, stack, sDbl, stackTop);
                                    continue;
                                 case 39:
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.name(cx, frame.scope, stringReg);
                                    continue;
                                 case 40:
                                    ++stackTop;
                                    stack[stackTop] = DBL_MRK;
                                    sDbl[stackTop] = frame.idata.itsDoubleTable[indexReg];
                                    continue;
                                 case 41:
                                    ++stackTop;
                                    stack[stackTop] = stringReg;
                                    continue;
                                 case 42:
                                    ++stackTop;
                                    stack[stackTop] = null;
                                    continue;
                                 case 43:
                                    ++stackTop;
                                    stack[stackTop] = frame.thisObj;
                                    continue;
                                 case 44:
                                    ++stackTop;
                                    stack[stackTop] = Boolean.FALSE;
                                    continue;
                                 case 45:
                                    ++stackTop;
                                    stack[stackTop] = Boolean.TRUE;
                                    continue;
                                 case 46:
                                 case 47:
                                    --stackTop;
                                    boolean valBln = doShallowEquals(stack, sDbl, stackTop);
                                    valBln ^= op == 47;
                                    stack[stackTop] = ScriptRuntime.wrapBoolean(valBln);
                                    continue;
                                 case 48:
                                    Object re = frame.idata.itsRegExpLiterals[indexReg];
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.wrapRegExp(cx, frame.scope, re);
                                    continue;
                                 case 49:
                                    ++stackTop;
                                    stack[stackTop] = ScriptRuntime.bind(cx, frame.scope, stringReg);
                                    continue;
                                 case 50:
                                    Object value = stack[stackTop];
                                    if (value == DBL_MRK) {
                                       value = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    int sourceLine = getIndex(iCode, frame.pc);
                                    throwable = new JavaScriptException(value, frame.idata.itsSourceFile, sourceLine);
                                    break label1141;
                                 case 51:
                                    indexReg += frame.localShift;
                                    throwable = stack[indexReg];
                                    break label1141;
                                 case 52:
                                 case 53:
                                    stackTop = doInOrInstanceof(cx, op, stack, sDbl, stackTop);
                                    continue;
                                 case 54:
                                    ++stackTop;
                                    indexReg += frame.localShift;
                                    stack[stackTop] = stack[indexReg];
                                    sDbl[stackTop] = sDbl[indexReg];
                                    continue;
                                 case 55:
                                    break label1075;
                                 case 56:
                                    break label1077;
                                 case 57:
                                    --stackTop;
                                    indexReg += frame.localShift;
                                    boolean afterFirstScope = frame.idata.itsICode[frame.pc] != 0;
                                    Throwable caughtException = (Throwable)stack[stackTop + 1];
                                    Scriptable lastCatchScope;
                                    if (!afterFirstScope) {
                                       lastCatchScope = null;
                                    } else {
                                       lastCatchScope = (Scriptable)stack[indexReg];
                                    }

                                    stack[indexReg] = ScriptRuntime.newCatchScope(caughtException, lastCatchScope, stringReg, cx, frame.scope);
                                    ++frame.pc;
                                    continue;
                                 case 58:
                                 case 59:
                                 case 60:
                                    Object lhs = stack[stackTop];
                                    if (lhs == DBL_MRK) {
                                       lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    indexReg += frame.localShift;
                                    int enumType = op == 58 ? 0 : (op == 59 ? 1 : 2);
                                    stack[indexReg] = ScriptRuntime.enumInit(lhs, cx, enumType);
                                    continue;
                                 case 61:
                                 case 62:
                                    indexReg += frame.localShift;
                                    Object val = stack[indexReg];
                                    ++stackTop;
                                    stack[stackTop] = op == 61 ? ScriptRuntime.enumNext(val) : ScriptRuntime.enumId(val, cx);
                                    continue;
                                 case 63:
                                    ++stackTop;
                                    stack[stackTop] = frame.fnOrScript;
                                    continue;
                                 case 64:
                                    break;
                                 case 67:
                                    Ref ref = (Ref)stack[stackTop];
                                    stack[stackTop] = ScriptRuntime.refGet(ref, cx);
                                    continue;
                                 case 68:
                                    Object value = stack[stackTop];
                                    if (value == DBL_MRK) {
                                       value = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    --stackTop;
                                    Ref ref = (Ref)stack[stackTop];
                                    stack[stackTop] = ScriptRuntime.refSet(ref, value, cx);
                                    continue;
                                 case 69:
                                    Ref ref = (Ref)stack[stackTop];
                                    stack[stackTop] = ScriptRuntime.refDel(ref, cx);
                                    continue;
                                 case 71:
                                    Object obj = stack[stackTop];
                                    if (obj == DBL_MRK) {
                                       obj = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.specialRef(obj, stringReg, cx);
                                    continue;
                                 case 74:
                                    Object value = stack[stackTop];
                                    if (value == DBL_MRK) {
                                       value = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.setDefaultNamespace(value, cx);
                                    continue;
                                 case 75:
                                    Object value = stack[stackTop];
                                    if (value != DBL_MRK) {
                                       stack[stackTop] = ScriptRuntime.escapeAttributeValue(value, cx);
                                    }
                                    continue;
                                 case 76:
                                    Object value = stack[stackTop];
                                    if (value != DBL_MRK) {
                                       stack[stackTop] = ScriptRuntime.escapeTextValue(value, cx);
                                    }
                                    continue;
                                 case 77:
                                    stackTop = doRefMember(cx, stack, sDbl, stackTop, indexReg);
                                    continue;
                                 case 78:
                                    stackTop = doRefNsMember(cx, stack, sDbl, stackTop, indexReg);
                                    continue;
                                 case 79:
                                    Object name = stack[stackTop];
                                    if (name == DBL_MRK) {
                                       name = ScriptRuntime.wrapNumber(sDbl[stackTop]);
                                    }

                                    stack[stackTop] = ScriptRuntime.nameRef(name, cx, frame.scope, indexReg);
                                    continue;
                                 case 80:
                                    stackTop = doRefNsName(cx, frame, stack, sDbl, stackTop, indexReg);
                                    continue;
                                 case 156:
                                    break label1079;
                              }

                              exitFrame(cx, frame, (Object)null);
                              interpreterResult = frame.result;
                              interpreterResultDbl = frame.resultDbl;
                              if (frame.parentFrame != null) {
                                 frame = frame.parentFrame;
                                 if (frame.frozen) {
                                    frame = frame.cloneFrozen();
                                 }

                                 setCallResult(frame, interpreterResult, interpreterResultDbl);
                                 interpreterResult = null;
                                 continue label1158;
                              }
                              break label1339;
                           }

                           if (instructionCounting) {
                              addInstructionCount(cx, frame, 2);
                           }

                           int offset = getShort(iCode, frame.pc);
                           if (offset != 0) {
                              frame.pc += offset - 1;
                           } else {
                              frame.pc = frame.idata.longJumps.getExistingInt(frame.pc);
                           }

                           if (instructionCounting) {
                              frame.pcPrevBranch = frame.pc;
                           }
                        }

                        stackTop = doGetVar(frame, stack, sDbl, stackTop, vars, varDbls, indexReg);
                     }

                     stackTop = doSetVar(frame, stack, sDbl, stackTop, vars, varDbls, varAttributes, indexReg);
                  }

                  stackTop = doSetConstVar(frame, stack, sDbl, stackTop, vars, varDbls, varAttributes, indexReg);
               }
            } catch (Throwable ex) {
               if (throwable != null) {
                  ex.printStackTrace(System.err);
                  throw new IllegalStateException();
               }

               throwable = ex;
            }

            if (throwable == null) {
               Kit.codeBug();
            }

            int EX_CATCH_STATE = 2;
            int EX_FINALLY_STATE = 1;
            int EX_NO_JS_STATE = 0;
            ContinuationJump cjump = null;
            int exState;
            if (generatorState != null && generatorState.operation == 2 && throwable == generatorState.value) {
               exState = 1;
            } else if (throwable instanceof JavaScriptException) {
               exState = 2;
            } else if (throwable instanceof EcmaError) {
               exState = 2;
            } else if (throwable instanceof EvaluatorException) {
               exState = 2;
            } else if (throwable instanceof RuntimeException) {
               exState = cx.hasFeature(13) ? 2 : 1;
            } else if (throwable instanceof Error) {
               exState = cx.hasFeature(13) ? 2 : 0;
            } else if (throwable instanceof ContinuationJump) {
               exState = 1;
               cjump = (ContinuationJump)throwable;
            } else {
               exState = cx.hasFeature(13) ? 2 : 1;
            }

            if (instructionCounting) {
               try {
                  addInstructionCount(cx, frame, 100);
               } catch (RuntimeException ex) {
                  throwable = ex;
                  exState = 1;
               } catch (Error ex) {
                  throwable = ex;
                  cjump = null;
                  exState = 0;
               }
            }

            if (frame.debuggerFrame != null && throwable instanceof RuntimeException) {
               RuntimeException rex = (RuntimeException)throwable;

               try {
                  frame.debuggerFrame.onExceptionThrown(cx, rex);
               } catch (Throwable ex) {
                  throwable = ex;
                  cjump = null;
                  exState = 0;
               }
            }

            do {
               if (exState != 0) {
                  boolean onlyFinally = exState != 2;
                  indexReg = getExceptionHandler(frame, onlyFinally);
                  if (indexReg >= 0) {
                     continue label1158;
                  }
               }

               exitFrame(cx, frame, throwable);
               frame = frame.parentFrame;
               if (frame == null) {
                  if (cjump == null) {
                     break label1339;
                  }

                  if (cjump.branchFrame != null) {
                     Kit.codeBug();
                  }

                  if (cjump.capturedFrame != null) {
                     indexReg = -1;
                     continue label1158;
                  } else {
                     interpreterResult = cjump.result;
                     interpreterResultDbl = cjump.resultDbl;
                     throwable = null;
                     break label1339;
                  }
               }
            } while(cjump == null || cjump.branchFrame != frame);

            indexReg = -1;
            continue;
         }

         if (cx.previousInterpreterInvocations != null && cx.previousInterpreterInvocations.size() != 0) {
            cx.lastInterpreterFrame = cx.previousInterpreterInvocations.pop();
         } else {
            cx.lastInterpreterFrame = null;
            cx.previousInterpreterInvocations = null;
         }

         if (throwable != null) {
            if (throwable instanceof RuntimeException) {
               throw (RuntimeException)throwable;
            }

            throw (Error)throwable;
         }

         return interpreterResult != DBL_MRK ? interpreterResult : ScriptRuntime.wrapNumber(interpreterResultDbl);
      }
   }

   private static int doInOrInstanceof(Context cx, int op, Object[] stack, double[] sDbl, int stackTop) {
      Object rhs = stack[stackTop];
      if (rhs == UniqueTag.DOUBLE_MARK) {
         rhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object lhs = stack[stackTop];
      if (lhs == UniqueTag.DOUBLE_MARK) {
         lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      boolean valBln;
      if (op == 52) {
         valBln = ScriptRuntime.in(lhs, rhs, cx);
      } else {
         valBln = ScriptRuntime.instanceOf(lhs, rhs, cx);
      }

      stack[stackTop] = ScriptRuntime.wrapBoolean(valBln);
      return stackTop;
   }

   private static int doCompare(CallFrame frame, int op, Object[] stack, double[] sDbl, int stackTop) {
      boolean valBln;
      label47: {
         --stackTop;
         Object rhs = stack[stackTop + 1];
         Object lhs = stack[stackTop];
         double rDbl;
         double lDbl;
         if (rhs == UniqueTag.DOUBLE_MARK) {
            rDbl = sDbl[stackTop + 1];
            lDbl = stack_double(frame, stackTop);
         } else {
            if (lhs != UniqueTag.DOUBLE_MARK) {
               switch (op) {
                  case 14:
                     valBln = ScriptRuntime.cmp_LT(lhs, rhs);
                     break label47;
                  case 15:
                     valBln = ScriptRuntime.cmp_LE(lhs, rhs);
                     break label47;
                  case 16:
                     valBln = ScriptRuntime.cmp_LT(rhs, lhs);
                     break label47;
                  case 17:
                     valBln = ScriptRuntime.cmp_LE(rhs, lhs);
                     break label47;
                  default:
                     throw Kit.codeBug();
               }
            }

            rDbl = ScriptRuntime.toNumber(rhs);
            lDbl = sDbl[stackTop];
         }

         switch (op) {
            case 14:
               valBln = lDbl < rDbl;
               break;
            case 15:
               valBln = lDbl <= rDbl;
               break;
            case 16:
               valBln = lDbl > rDbl;
               break;
            case 17:
               valBln = lDbl >= rDbl;
               break;
            default:
               throw Kit.codeBug();
         }
      }

      stack[stackTop] = ScriptRuntime.wrapBoolean(valBln);
      return stackTop;
   }

   private static int doBitOp(CallFrame frame, int op, Object[] stack, double[] sDbl, int stackTop) {
      int lIntValue = stack_int32(frame, stackTop - 1);
      int rIntValue = stack_int32(frame, stackTop);
      --stackTop;
      stack[stackTop] = UniqueTag.DOUBLE_MARK;
      switch (op) {
         case 9:
            lIntValue |= rIntValue;
            break;
         case 10:
            lIntValue ^= rIntValue;
            break;
         case 11:
            lIntValue &= rIntValue;
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         default:
            break;
         case 18:
            lIntValue <<= rIntValue;
            break;
         case 19:
            lIntValue >>= rIntValue;
      }

      sDbl[stackTop] = (double)lIntValue;
      return stackTop;
   }

   private static int doDelName(Context cx, int op, Object[] stack, double[] sDbl, int stackTop) {
      Object rhs = stack[stackTop];
      if (rhs == UniqueTag.DOUBLE_MARK) {
         rhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object lhs = stack[stackTop];
      if (lhs == UniqueTag.DOUBLE_MARK) {
         lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      stack[stackTop] = ScriptRuntime.delete(lhs, rhs, cx, op == 0);
      return stackTop;
   }

   private static int doGetElem(Context cx, CallFrame frame, Object[] stack, double[] sDbl, int stackTop) {
      --stackTop;
      Object lhs = stack[stackTop];
      if (lhs == UniqueTag.DOUBLE_MARK) {
         lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      Object id = stack[stackTop + 1];
      Object value;
      if (id != UniqueTag.DOUBLE_MARK) {
         value = ScriptRuntime.getObjectElem(lhs, id, cx, frame.scope);
      } else {
         double d = sDbl[stackTop + 1];
         value = ScriptRuntime.getObjectIndex(lhs, d, cx);
      }

      stack[stackTop] = value;
      return stackTop;
   }

   private static int doSetElem(Context cx, Object[] stack, double[] sDbl, int stackTop) {
      stackTop -= 2;
      Object rhs = stack[stackTop + 2];
      if (rhs == UniqueTag.DOUBLE_MARK) {
         rhs = ScriptRuntime.wrapNumber(sDbl[stackTop + 2]);
      }

      Object lhs = stack[stackTop];
      if (lhs == UniqueTag.DOUBLE_MARK) {
         lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      Object id = stack[stackTop + 1];
      Object value;
      if (id != UniqueTag.DOUBLE_MARK) {
         value = ScriptRuntime.setObjectElem(lhs, id, rhs, cx);
      } else {
         double d = sDbl[stackTop + 1];
         value = ScriptRuntime.setObjectIndex(lhs, d, rhs, cx);
      }

      stack[stackTop] = value;
      return stackTop;
   }

   private static int doElemIncDec(Context cx, CallFrame frame, byte[] iCode, Object[] stack, double[] sDbl, int stackTop) {
      Object rhs = stack[stackTop];
      if (rhs == UniqueTag.DOUBLE_MARK) {
         rhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object lhs = stack[stackTop];
      if (lhs == UniqueTag.DOUBLE_MARK) {
         lhs = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      stack[stackTop] = ScriptRuntime.elemIncrDecr(lhs, rhs, cx, iCode[frame.pc]);
      ++frame.pc;
      return stackTop;
   }

   private static int doCallSpecial(Context cx, CallFrame frame, Object[] stack, double[] sDbl, int stackTop, byte[] iCode, int indexReg) {
      int callType = iCode[frame.pc] & 255;
      boolean isNew = iCode[frame.pc + 1] != 0;
      int sourceLine = getIndex(iCode, frame.pc + 2);
      if (isNew) {
         stackTop -= indexReg;
         Object function = stack[stackTop];
         if (function == UniqueTag.DOUBLE_MARK) {
            function = ScriptRuntime.wrapNumber(sDbl[stackTop]);
         }

         Object[] outArgs = getArgsArray(stack, sDbl, stackTop + 1, indexReg);
         stack[stackTop] = ScriptRuntime.newSpecial(cx, function, outArgs, frame.scope, callType);
      } else {
         stackTop -= 1 + indexReg;
         Scriptable functionThis = (Scriptable)stack[stackTop + 1];
         Callable function = (Callable)stack[stackTop];
         Object[] outArgs = getArgsArray(stack, sDbl, stackTop + 2, indexReg);
         stack[stackTop] = ScriptRuntime.callSpecial(cx, function, functionThis, outArgs, frame.scope, frame.thisObj, callType, frame.idata.itsSourceFile, sourceLine);
      }

      frame.pc += 4;
      return stackTop;
   }

   private static int doSetConstVar(CallFrame frame, Object[] stack, double[] sDbl, int stackTop, Object[] vars, double[] varDbls, int[] varAttributes, int indexReg) {
      if (!frame.useActivation) {
         if ((varAttributes[indexReg] & 1) == 0) {
            throw Context.reportRuntimeError1("msg.var.redecl", frame.idata.argNames[indexReg]);
         }

         if ((varAttributes[indexReg] & 8) != 0) {
            vars[indexReg] = stack[stackTop];
            varAttributes[indexReg] &= -9;
            varDbls[indexReg] = sDbl[stackTop];
         }
      } else {
         Object val = stack[stackTop];
         if (val == UniqueTag.DOUBLE_MARK) {
            val = ScriptRuntime.wrapNumber(sDbl[stackTop]);
         }

         String stringReg = frame.idata.argNames[indexReg];
         if (!(frame.scope instanceof ConstProperties)) {
            throw Kit.codeBug();
         }

         ConstProperties cp = (ConstProperties)frame.scope;
         cp.putConst(stringReg, frame.scope, val);
      }

      return stackTop;
   }

   private static int doSetVar(CallFrame frame, Object[] stack, double[] sDbl, int stackTop, Object[] vars, double[] varDbls, int[] varAttributes, int indexReg) {
      if (!frame.useActivation) {
         if ((varAttributes[indexReg] & 1) == 0) {
            vars[indexReg] = stack[stackTop];
            varDbls[indexReg] = sDbl[stackTop];
         }
      } else {
         Object val = stack[stackTop];
         if (val == UniqueTag.DOUBLE_MARK) {
            val = ScriptRuntime.wrapNumber(sDbl[stackTop]);
         }

         String stringReg = frame.idata.argNames[indexReg];
         frame.scope.put(stringReg, frame.scope, val);
      }

      return stackTop;
   }

   private static int doGetVar(CallFrame frame, Object[] stack, double[] sDbl, int stackTop, Object[] vars, double[] varDbls, int indexReg) {
      ++stackTop;
      if (!frame.useActivation) {
         stack[stackTop] = vars[indexReg];
         sDbl[stackTop] = varDbls[indexReg];
      } else {
         String stringReg = frame.idata.argNames[indexReg];
         stack[stackTop] = frame.scope.get(stringReg, frame.scope);
      }

      return stackTop;
   }

   private static int doVarIncDec(Context cx, CallFrame frame, Object[] stack, double[] sDbl, int stackTop, Object[] vars, double[] varDbls, int indexReg) {
      ++stackTop;
      int incrDecrMask = frame.idata.itsICode[frame.pc];
      if (!frame.useActivation) {
         stack[stackTop] = UniqueTag.DOUBLE_MARK;
         Object varValue = vars[indexReg];
         double d;
         if (varValue == UniqueTag.DOUBLE_MARK) {
            d = varDbls[indexReg];
         } else {
            d = ScriptRuntime.toNumber(varValue);
            vars[indexReg] = UniqueTag.DOUBLE_MARK;
         }

         double d2 = (incrDecrMask & 1) == 0 ? d + (double)1.0F : d - (double)1.0F;
         varDbls[indexReg] = d2;
         sDbl[stackTop] = (incrDecrMask & 2) == 0 ? d2 : d;
      } else {
         String varName = frame.idata.argNames[indexReg];
         stack[stackTop] = ScriptRuntime.nameIncrDecr(frame.scope, varName, cx, incrDecrMask);
      }

      ++frame.pc;
      return stackTop;
   }

   private static int doRefMember(Context cx, Object[] stack, double[] sDbl, int stackTop, int flags) {
      Object elem = stack[stackTop];
      if (elem == UniqueTag.DOUBLE_MARK) {
         elem = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object obj = stack[stackTop];
      if (obj == UniqueTag.DOUBLE_MARK) {
         obj = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      stack[stackTop] = ScriptRuntime.memberRef(obj, elem, cx, flags);
      return stackTop;
   }

   private static int doRefNsMember(Context cx, Object[] stack, double[] sDbl, int stackTop, int flags) {
      Object elem = stack[stackTop];
      if (elem == UniqueTag.DOUBLE_MARK) {
         elem = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object ns = stack[stackTop];
      if (ns == UniqueTag.DOUBLE_MARK) {
         ns = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object obj = stack[stackTop];
      if (obj == UniqueTag.DOUBLE_MARK) {
         obj = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      stack[stackTop] = ScriptRuntime.memberRef(obj, ns, elem, cx, flags);
      return stackTop;
   }

   private static int doRefNsName(Context cx, CallFrame frame, Object[] stack, double[] sDbl, int stackTop, int flags) {
      Object name = stack[stackTop];
      if (name == UniqueTag.DOUBLE_MARK) {
         name = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      --stackTop;
      Object ns = stack[stackTop];
      if (ns == UniqueTag.DOUBLE_MARK) {
         ns = ScriptRuntime.wrapNumber(sDbl[stackTop]);
      }

      stack[stackTop] = ScriptRuntime.nameRef(ns, name, cx, frame.scope, flags);
      return stackTop;
   }

   private static CallFrame initFrameForNoSuchMethod(Context cx, CallFrame frame, int indexReg, Object[] stack, double[] sDbl, int stackTop, int op, Scriptable funThisObj, Scriptable calleeScope, ScriptRuntime.NoSuchMethodShim noSuchMethodShim, InterpretedFunction ifun) {
      Object[] argsArray = null;
      int shift = stackTop + 2;
      Object[] elements = new Object[indexReg];

      for(int i = 0; i < indexReg; ++shift) {
         Object val = stack[shift];
         if (val == UniqueTag.DOUBLE_MARK) {
            val = ScriptRuntime.wrapNumber(sDbl[shift]);
         }

         elements[i] = val;
         ++i;
      }

      argsArray = new Object[]{noSuchMethodShim.methodName, cx.newArray(calleeScope, elements)};
      CallFrame callParentFrame = frame;
      CallFrame calleeFrame = new CallFrame();
      if (op == -55) {
         callParentFrame = frame.parentFrame;
         exitFrame(cx, frame, (Object)null);
      }

      initFrame(cx, calleeScope, funThisObj, argsArray, (double[])null, 0, 2, ifun, callParentFrame, calleeFrame);
      if (op != -55) {
         frame.savedStackTop = stackTop;
         frame.savedCallOp = op;
      }

      return calleeFrame;
   }

   private static boolean doEquals(Object[] stack, double[] sDbl, int stackTop) {
      Object rhs = stack[stackTop + 1];
      Object lhs = stack[stackTop];
      if (rhs == UniqueTag.DOUBLE_MARK) {
         if (lhs == UniqueTag.DOUBLE_MARK) {
            return sDbl[stackTop] == sDbl[stackTop + 1];
         } else {
            return ScriptRuntime.eqNumber(sDbl[stackTop + 1], lhs);
         }
      } else {
         return lhs == UniqueTag.DOUBLE_MARK ? ScriptRuntime.eqNumber(sDbl[stackTop], rhs) : ScriptRuntime.eq(lhs, rhs);
      }
   }

   private static boolean doShallowEquals(Object[] stack, double[] sDbl, int stackTop) {
      Object rhs = stack[stackTop + 1];
      Object lhs = stack[stackTop];
      Object DBL_MRK = UniqueTag.DOUBLE_MARK;
      double rdbl;
      double ldbl;
      if (rhs == DBL_MRK) {
         rdbl = sDbl[stackTop + 1];
         if (lhs == DBL_MRK) {
            ldbl = sDbl[stackTop];
         } else {
            if (!(lhs instanceof Number)) {
               return false;
            }

            ldbl = ((Number)lhs).doubleValue();
         }
      } else {
         if (lhs != DBL_MRK) {
            return ScriptRuntime.shallowEq(lhs, rhs);
         }

         ldbl = sDbl[stackTop];
         if (!(rhs instanceof Number)) {
            return false;
         }

         rdbl = ((Number)rhs).doubleValue();
      }

      return ldbl == rdbl;
   }

   private static CallFrame processThrowable(Context cx, Object throwable, CallFrame frame, int indexReg, boolean instructionCounting) {
      if (indexReg >= 0) {
         if (frame.frozen) {
            frame = frame.cloneFrozen();
         }

         int[] table = frame.idata.itsExceptionTable;
         frame.pc = table[indexReg + 2];
         if (instructionCounting) {
            frame.pcPrevBranch = frame.pc;
         }

         frame.savedStackTop = frame.emptyStackTop;
         int scopeLocal = frame.localShift + table[indexReg + 5];
         int exLocal = frame.localShift + table[indexReg + 4];
         frame.scope = (Scriptable)frame.stack[scopeLocal];
         frame.stack[exLocal] = throwable;
         throwable = null;
      } else {
         ContinuationJump cjump = (ContinuationJump)throwable;
         throwable = null;
         if (cjump.branchFrame != frame) {
            Kit.codeBug();
         }

         if (cjump.capturedFrame == null) {
            Kit.codeBug();
         }

         int rewindCount = cjump.capturedFrame.frameIndex + 1;
         if (cjump.branchFrame != null) {
            rewindCount -= cjump.branchFrame.frameIndex;
         }

         int enterCount = 0;
         CallFrame[] enterFrames = null;
         CallFrame x = cjump.capturedFrame;

         for(int i = 0; i != rewindCount; ++i) {
            if (!x.frozen) {
               Kit.codeBug();
            }

            if (isFrameEnterExitRequired(x)) {
               if (enterFrames == null) {
                  enterFrames = new CallFrame[rewindCount - i];
               }

               enterFrames[enterCount] = x;
               ++enterCount;
            }

            x = x.parentFrame;
         }

         while(enterCount != 0) {
            --enterCount;
            x = enterFrames[enterCount];
            enterFrame(cx, x, ScriptRuntime.emptyArgs, true);
         }

         frame = cjump.capturedFrame.cloneFrozen();
         setCallResult(frame, cjump.result, cjump.resultDbl);
      }

      frame.throwable = throwable;
      return frame;
   }

   private static Object freezeGenerator(Context cx, CallFrame frame, int stackTop, GeneratorState generatorState) {
      if (generatorState.operation == 2) {
         throw ScriptRuntime.typeError0("msg.yield.closing");
      } else {
         frame.frozen = true;
         frame.result = frame.stack[stackTop];
         frame.resultDbl = frame.sDbl[stackTop];
         frame.savedStackTop = stackTop;
         --frame.pc;
         ScriptRuntime.exitActivationFunction(cx);
         return frame.result != UniqueTag.DOUBLE_MARK ? frame.result : ScriptRuntime.wrapNumber(frame.resultDbl);
      }
   }

   private static Object thawGenerator(CallFrame frame, int stackTop, GeneratorState generatorState, int op) {
      frame.frozen = false;
      int sourceLine = getIndex(frame.idata.itsICode, frame.pc);
      frame.pc += 2;
      if (generatorState.operation == 1) {
         return new JavaScriptException(generatorState.value, frame.idata.itsSourceFile, sourceLine);
      } else if (generatorState.operation == 2) {
         return generatorState.value;
      } else if (generatorState.operation != 0) {
         throw Kit.codeBug();
      } else {
         if (op == 72) {
            frame.stack[stackTop] = generatorState.value;
         }

         return Scriptable.NOT_FOUND;
      }
   }

   private static CallFrame initFrameForApplyOrCall(Context cx, CallFrame frame, int indexReg, Object[] stack, double[] sDbl, int stackTop, int op, Scriptable calleeScope, IdFunctionObject ifun, InterpretedFunction iApplyCallable) {
      Scriptable applyThis;
      if (indexReg != 0) {
         Object obj = stack[stackTop + 2];
         if (obj == UniqueTag.DOUBLE_MARK) {
            obj = ScriptRuntime.wrapNumber(sDbl[stackTop + 2]);
         }

         applyThis = ScriptRuntime.toObjectOrNull(cx, obj);
      } else {
         applyThis = null;
      }

      if (applyThis == null) {
         applyThis = ScriptRuntime.getTopCallScope(cx);
      }

      if (op == -55) {
         exitFrame(cx, frame, (Object)null);
         frame = frame.parentFrame;
      } else {
         frame.savedStackTop = stackTop;
         frame.savedCallOp = op;
      }

      CallFrame calleeFrame = new CallFrame();
      if (BaseFunction.isApply(ifun)) {
         Object[] callArgs = indexReg < 2 ? ScriptRuntime.emptyArgs : ScriptRuntime.getApplyArguments(cx, stack[stackTop + 3]);
         initFrame(cx, calleeScope, applyThis, callArgs, (double[])null, 0, callArgs.length, iApplyCallable, frame, calleeFrame);
      } else {
         for(int i = 1; i < indexReg; ++i) {
            stack[stackTop + 1 + i] = stack[stackTop + 2 + i];
            sDbl[stackTop + 1 + i] = sDbl[stackTop + 2 + i];
         }

         int argCount = indexReg < 2 ? 0 : indexReg - 1;
         initFrame(cx, calleeScope, applyThis, stack, sDbl, stackTop + 2, argCount, iApplyCallable, frame, calleeFrame);
      }

      return calleeFrame;
   }

   private static void initFrame(Context cx, Scriptable callerScope, Scriptable thisObj, Object[] args, double[] argsDbl, int argShift, int argCount, InterpretedFunction fnOrScript, CallFrame parentFrame, CallFrame frame) {
      InterpreterData idata = fnOrScript.idata;
      boolean useActivation = idata.itsNeedsActivation;
      DebugFrame debuggerFrame = null;
      if (cx.debugger != null) {
         debuggerFrame = cx.debugger.getFrame(cx, idata);
         if (debuggerFrame != null) {
            useActivation = true;
         }
      }

      if (useActivation) {
         if (argsDbl != null) {
            args = getArgsArray(args, argsDbl, argShift, argCount);
         }

         argShift = 0;
         argsDbl = null;
      }

      Scriptable scope;
      if (idata.itsFunctionType != 0) {
         scope = fnOrScript.getParentScope();
         if (useActivation) {
            scope = ScriptRuntime.createFunctionActivation(fnOrScript, scope, args);
         }
      } else {
         scope = callerScope;
         ScriptRuntime.initScript(fnOrScript, thisObj, cx, callerScope, fnOrScript.idata.evalScriptFlag);
      }

      if (idata.itsNestedFunctions != null) {
         if (idata.itsFunctionType != 0 && !idata.itsNeedsActivation) {
            Kit.codeBug();
         }

         for(int i = 0; i < idata.itsNestedFunctions.length; ++i) {
            InterpreterData fdata = idata.itsNestedFunctions[i];
            if (fdata.itsFunctionType == 1) {
               initFunction(cx, scope, fnOrScript, i);
            }
         }
      }

      int emptyStackTop = idata.itsMaxVars + idata.itsMaxLocals - 1;
      int maxFrameArray = idata.itsMaxFrameArray;
      if (maxFrameArray != emptyStackTop + idata.itsMaxStack + 1) {
         Kit.codeBug();
      }

      boolean stackReuse;
      Object[] stack;
      int[] stackAttributes;
      double[] sDbl;
      if (frame.stack != null && maxFrameArray <= frame.stack.length) {
         stackReuse = true;
         stack = frame.stack;
         stackAttributes = frame.stackAttributes;
         sDbl = frame.sDbl;
      } else {
         stackReuse = false;
         stack = new Object[maxFrameArray];
         stackAttributes = new int[maxFrameArray];
         sDbl = new double[maxFrameArray];
      }

      int varCount = idata.getParamAndVarCount();

      for(int i = 0; i < varCount; ++i) {
         if (idata.getParamOrVarConst(i)) {
            stackAttributes[i] = 13;
         }
      }

      int definedArgs = idata.argCount;
      if (definedArgs > argCount) {
         definedArgs = argCount;
      }

      frame.parentFrame = parentFrame;
      frame.frameIndex = parentFrame == null ? 0 : parentFrame.frameIndex + 1;
      if (frame.frameIndex > cx.getMaximumInterpreterStackDepth()) {
         throw Context.reportRuntimeError("Exceeded maximum stack depth");
      } else {
         frame.frozen = false;
         frame.fnOrScript = fnOrScript;
         frame.idata = idata;
         frame.stack = stack;
         frame.stackAttributes = stackAttributes;
         frame.sDbl = sDbl;
         frame.varSource = frame;
         frame.localShift = idata.itsMaxVars;
         frame.emptyStackTop = emptyStackTop;
         frame.debuggerFrame = debuggerFrame;
         frame.useActivation = useActivation;
         frame.thisObj = thisObj;
         frame.result = Undefined.instance;
         frame.pc = 0;
         frame.pcPrevBranch = 0;
         frame.pcSourceLineStart = idata.firstLinePC;
         frame.scope = scope;
         frame.savedStackTop = emptyStackTop;
         frame.savedCallOp = 0;
         System.arraycopy(args, argShift, stack, 0, definedArgs);
         if (argsDbl != null) {
            System.arraycopy(argsDbl, argShift, sDbl, 0, definedArgs);
         }

         for(int i = definedArgs; i != idata.itsMaxVars; ++i) {
            stack[i] = Undefined.instance;
         }

         if (stackReuse) {
            for(int i = emptyStackTop + 1; i != stack.length; ++i) {
               stack[i] = null;
            }
         }

         enterFrame(cx, frame, args, false);
      }
   }

   private static boolean isFrameEnterExitRequired(CallFrame frame) {
      return frame.debuggerFrame != null || frame.idata.itsNeedsActivation;
   }

   private static void enterFrame(Context cx, CallFrame frame, Object[] args, boolean continuationRestart) {
      boolean usesActivation = frame.idata.itsNeedsActivation;
      boolean isDebugged = frame.debuggerFrame != null;
      if (usesActivation || isDebugged) {
         Scriptable scope = frame.scope;
         if (scope == null) {
            Kit.codeBug();
         } else if (continuationRestart) {
            while(scope instanceof NativeWith) {
               scope = scope.getParentScope();
               if (scope == null || frame.parentFrame != null && frame.parentFrame.scope == scope) {
                  Kit.codeBug();
                  break;
               }
            }
         }

         if (isDebugged) {
            frame.debuggerFrame.onEnter(cx, scope, frame.thisObj, args);
         }

         if (usesActivation) {
            ScriptRuntime.enterActivationFunction(cx, scope);
         }
      }

   }

   private static void exitFrame(Context cx, CallFrame frame, Object throwable) {
      if (frame.idata.itsNeedsActivation) {
         ScriptRuntime.exitActivationFunction(cx);
      }

      if (frame.debuggerFrame != null) {
         try {
            if (throwable instanceof Throwable) {
               frame.debuggerFrame.onExit(cx, true, throwable);
            } else {
               ContinuationJump cjump = (ContinuationJump)throwable;
               Object result;
               if (cjump == null) {
                  result = frame.result;
               } else {
                  result = cjump.result;
               }

               if (result == UniqueTag.DOUBLE_MARK) {
                  double resultDbl;
                  if (cjump == null) {
                     resultDbl = frame.resultDbl;
                  } else {
                     resultDbl = cjump.resultDbl;
                  }

                  result = ScriptRuntime.wrapNumber(resultDbl);
               }

               frame.debuggerFrame.onExit(cx, false, result);
            }
         } catch (Throwable ex) {
            System.err.println("RHINO USAGE WARNING: onExit terminated with exception");
            ex.printStackTrace(System.err);
         }
      }

   }

   private static void setCallResult(CallFrame frame, Object callResult, double callResultDbl) {
      if (frame.savedCallOp == 38) {
         frame.stack[frame.savedStackTop] = callResult;
         frame.sDbl[frame.savedStackTop] = callResultDbl;
      } else if (frame.savedCallOp == 30) {
         if (callResult instanceof Scriptable) {
            frame.stack[frame.savedStackTop] = callResult;
         }
      } else {
         Kit.codeBug();
      }

      frame.savedCallOp = 0;
   }

   public static NativeContinuation captureContinuation(Context cx) {
      if (cx.lastInterpreterFrame != null && cx.lastInterpreterFrame instanceof CallFrame) {
         return captureContinuation(cx, (CallFrame)cx.lastInterpreterFrame, true);
      } else {
         throw new IllegalStateException("Interpreter frames not found");
      }
   }

   private static NativeContinuation captureContinuation(Context cx, CallFrame frame, boolean requireContinuationsTopFrame) {
      NativeContinuation c = new NativeContinuation();
      ScriptRuntime.setObjectProtoAndParent(c, ScriptRuntime.getTopCallScope(cx));
      CallFrame x = frame;

      CallFrame outermost;
      for(outermost = frame; x != null && !x.frozen; x = x.parentFrame) {
         x.frozen = true;

         for(int i = x.savedStackTop + 1; i != x.stack.length; ++i) {
            x.stack[i] = null;
            x.stackAttributes[i] = 0;
         }

         if (x.savedCallOp == 38) {
            x.stack[x.savedStackTop] = null;
         } else if (x.savedCallOp != 30) {
            Kit.codeBug();
         }

         outermost = x;
      }

      if (requireContinuationsTopFrame) {
         while(outermost.parentFrame != null) {
            outermost = outermost.parentFrame;
         }

         if (!outermost.isContinuationsTopFrame) {
            throw new IllegalStateException("Cannot capture continuation from JavaScript code not called directly by executeScriptWithContinuations or callFunctionWithContinuations");
         }
      }

      c.initImplementation(frame);
      return c;
   }

   private static int stack_int32(CallFrame frame, int i) {
      Object x = frame.stack[i];
      return x == UniqueTag.DOUBLE_MARK ? ScriptRuntime.toInt32(frame.sDbl[i]) : ScriptRuntime.toInt32(x);
   }

   private static double stack_double(CallFrame frame, int i) {
      Object x = frame.stack[i];
      return x != UniqueTag.DOUBLE_MARK ? ScriptRuntime.toNumber(x) : frame.sDbl[i];
   }

   private static boolean stack_boolean(CallFrame frame, int i) {
      Object x = frame.stack[i];
      if (x == Boolean.TRUE) {
         return true;
      } else if (x == Boolean.FALSE) {
         return false;
      } else if (x == UniqueTag.DOUBLE_MARK) {
         double d = frame.sDbl[i];
         return d == d && d != (double)0.0F;
      } else if (x != null && x != Undefined.instance) {
         if (!(x instanceof Number)) {
            return x instanceof Boolean ? (Boolean)x : ScriptRuntime.toBoolean(x);
         } else {
            double d = ((Number)x).doubleValue();
            return d == d && d != (double)0.0F;
         }
      } else {
         return false;
      }
   }

   private static void doAdd(Object[] stack, double[] sDbl, int stackTop, Context cx) {
      Object rhs = stack[stackTop + 1];
      Object lhs = stack[stackTop];
      double d;
      boolean leftRightOrder;
      if (rhs == UniqueTag.DOUBLE_MARK) {
         d = sDbl[stackTop + 1];
         if (lhs == UniqueTag.DOUBLE_MARK) {
            sDbl[stackTop] += d;
            return;
         }

         leftRightOrder = true;
      } else {
         if (lhs != UniqueTag.DOUBLE_MARK) {
            if (!(lhs instanceof Scriptable) && !(rhs instanceof Scriptable)) {
               if (!(lhs instanceof CharSequence) && !(rhs instanceof CharSequence)) {
                  double lDbl = lhs instanceof Number ? ((Number)lhs).doubleValue() : ScriptRuntime.toNumber(lhs);
                  double rDbl = rhs instanceof Number ? ((Number)rhs).doubleValue() : ScriptRuntime.toNumber(rhs);
                  stack[stackTop] = UniqueTag.DOUBLE_MARK;
                  sDbl[stackTop] = lDbl + rDbl;
               } else {
                  CharSequence lstr = ScriptRuntime.toCharSequence(lhs);
                  CharSequence rstr = ScriptRuntime.toCharSequence(rhs);
                  stack[stackTop] = new ConsString(lstr, rstr);
               }
            } else {
               stack[stackTop] = ScriptRuntime.add(lhs, rhs, cx);
            }

            return;
         }

         d = sDbl[stackTop];
         lhs = rhs;
         leftRightOrder = false;
      }

      if (lhs instanceof Scriptable) {
         rhs = ScriptRuntime.wrapNumber(d);
         if (!leftRightOrder) {
            Object tmp = lhs;
            lhs = rhs;
            rhs = tmp;
         }

         stack[stackTop] = ScriptRuntime.add(lhs, rhs, cx);
      } else if (lhs instanceof CharSequence) {
         CharSequence lstr = (CharSequence)lhs;
         CharSequence rstr = ScriptRuntime.toCharSequence(d);
         if (leftRightOrder) {
            stack[stackTop] = new ConsString(lstr, rstr);
         } else {
            stack[stackTop] = new ConsString(rstr, lstr);
         }
      } else {
         double lDbl = lhs instanceof Number ? ((Number)lhs).doubleValue() : ScriptRuntime.toNumber(lhs);
         stack[stackTop] = UniqueTag.DOUBLE_MARK;
         sDbl[stackTop] = lDbl + d;
      }

   }

   private static int doArithmetic(CallFrame frame, int op, Object[] stack, double[] sDbl, int stackTop) {
      double rDbl = stack_double(frame, stackTop);
      --stackTop;
      double lDbl = stack_double(frame, stackTop);
      stack[stackTop] = UniqueTag.DOUBLE_MARK;
      switch (op) {
         case 22:
            lDbl -= rDbl;
            break;
         case 23:
            lDbl *= rDbl;
            break;
         case 24:
            lDbl /= rDbl;
            break;
         case 25:
            lDbl %= rDbl;
      }

      sDbl[stackTop] = lDbl;
      return stackTop;
   }

   private static Object[] getArgsArray(Object[] stack, double[] sDbl, int shift, int count) {
      if (count == 0) {
         return ScriptRuntime.emptyArgs;
      } else {
         Object[] args = new Object[count];

         for(int i = 0; i != count; ++shift) {
            Object val = stack[shift];
            if (val == UniqueTag.DOUBLE_MARK) {
               val = ScriptRuntime.wrapNumber(sDbl[shift]);
            }

            args[i] = val;
            ++i;
         }

         return args;
      }
   }

   private static void addInstructionCount(Context cx, CallFrame frame, int extra) {
      cx.instructionCount += frame.pc - frame.pcPrevBranch + extra;
      if (cx.instructionCount > cx.instructionThreshold) {
         cx.observeInstructionCount(cx.instructionCount);
         cx.instructionCount = 0;
      }

   }

   private static class CallFrame implements Cloneable, Serializable {
      static final long serialVersionUID = -2843792508994958978L;
      CallFrame parentFrame;
      int frameIndex;
      boolean frozen;
      InterpretedFunction fnOrScript;
      InterpreterData idata;
      Object[] stack;
      int[] stackAttributes;
      double[] sDbl;
      CallFrame varSource;
      int localShift;
      int emptyStackTop;
      DebugFrame debuggerFrame;
      boolean useActivation;
      boolean isContinuationsTopFrame;
      Scriptable thisObj;
      Object result;
      double resultDbl;
      int pc;
      int pcPrevBranch;
      int pcSourceLineStart;
      Scriptable scope;
      int savedStackTop;
      int savedCallOp;
      Object throwable;

      private CallFrame() {
         super();
      }

      CallFrame cloneFrozen() {
         if (!this.frozen) {
            Kit.codeBug();
         }

         CallFrame copy;
         try {
            copy = (CallFrame)this.clone();
         } catch (CloneNotSupportedException var3) {
            throw new IllegalStateException();
         }

         copy.stack = this.stack.clone();
         copy.stackAttributes = (int[])this.stackAttributes.clone();
         copy.sDbl = (double[])this.sDbl.clone();
         copy.frozen = false;
         return copy;
      }
   }

   private static final class ContinuationJump implements Serializable {
      static final long serialVersionUID = 7687739156004308247L;
      CallFrame capturedFrame;
      CallFrame branchFrame;
      Object result;
      double resultDbl;

      ContinuationJump(NativeContinuation c, CallFrame current) {
         super();
         this.capturedFrame = (CallFrame)c.getImplementation();
         if (this.capturedFrame != null && current != null) {
            CallFrame chain1 = this.capturedFrame;
            CallFrame chain2 = current;
            int diff = chain1.frameIndex - current.frameIndex;
            if (diff != 0) {
               if (diff < 0) {
                  chain1 = current;
                  chain2 = this.capturedFrame;
                  diff = -diff;
               }

               do {
                  chain1 = chain1.parentFrame;
                  --diff;
               } while(diff != 0);

               if (chain1.frameIndex != chain2.frameIndex) {
                  Kit.codeBug();
               }
            }

            while(chain1 != chain2 && chain1 != null) {
               chain1 = chain1.parentFrame;
               chain2 = chain2.parentFrame;
            }

            this.branchFrame = chain1;
            if (this.branchFrame != null && !this.branchFrame.frozen) {
               Kit.codeBug();
            }
         } else {
            this.branchFrame = null;
         }

      }
   }

   static class GeneratorState {
      int operation;
      Object value;
      RuntimeException returnedException;

      GeneratorState(int operation, Object value) {
         super();
         this.operation = operation;
         this.value = value;
      }
   }
}
