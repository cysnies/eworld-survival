package org.mozilla.javascript;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

final class NativeDate extends IdScriptableObject {
   static final long serialVersionUID = -8307438915861678966L;
   private static final Object DATE_TAG = "Date";
   private static final String js_NaN_date_str = "Invalid Date";
   private static final DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
   private static final double HalfTimeDomain = 8.64E15;
   private static final double HoursPerDay = (double)24.0F;
   private static final double MinutesPerHour = (double)60.0F;
   private static final double SecondsPerMinute = (double)60.0F;
   private static final double msPerSecond = (double)1000.0F;
   private static final double MinutesPerDay = (double)1440.0F;
   private static final double SecondsPerDay = (double)86400.0F;
   private static final double SecondsPerHour = (double)3600.0F;
   private static final double msPerDay = (double)8.64E7F;
   private static final double msPerHour = (double)3600000.0F;
   private static final double msPerMinute = (double)60000.0F;
   private static final int MAXARGS = 7;
   private static final int ConstructorId_now = -3;
   private static final int ConstructorId_parse = -2;
   private static final int ConstructorId_UTC = -1;
   private static final int Id_constructor = 1;
   private static final int Id_toString = 2;
   private static final int Id_toTimeString = 3;
   private static final int Id_toDateString = 4;
   private static final int Id_toLocaleString = 5;
   private static final int Id_toLocaleTimeString = 6;
   private static final int Id_toLocaleDateString = 7;
   private static final int Id_toUTCString = 8;
   private static final int Id_toSource = 9;
   private static final int Id_valueOf = 10;
   private static final int Id_getTime = 11;
   private static final int Id_getYear = 12;
   private static final int Id_getFullYear = 13;
   private static final int Id_getUTCFullYear = 14;
   private static final int Id_getMonth = 15;
   private static final int Id_getUTCMonth = 16;
   private static final int Id_getDate = 17;
   private static final int Id_getUTCDate = 18;
   private static final int Id_getDay = 19;
   private static final int Id_getUTCDay = 20;
   private static final int Id_getHours = 21;
   private static final int Id_getUTCHours = 22;
   private static final int Id_getMinutes = 23;
   private static final int Id_getUTCMinutes = 24;
   private static final int Id_getSeconds = 25;
   private static final int Id_getUTCSeconds = 26;
   private static final int Id_getMilliseconds = 27;
   private static final int Id_getUTCMilliseconds = 28;
   private static final int Id_getTimezoneOffset = 29;
   private static final int Id_setTime = 30;
   private static final int Id_setMilliseconds = 31;
   private static final int Id_setUTCMilliseconds = 32;
   private static final int Id_setSeconds = 33;
   private static final int Id_setUTCSeconds = 34;
   private static final int Id_setMinutes = 35;
   private static final int Id_setUTCMinutes = 36;
   private static final int Id_setHours = 37;
   private static final int Id_setUTCHours = 38;
   private static final int Id_setDate = 39;
   private static final int Id_setUTCDate = 40;
   private static final int Id_setMonth = 41;
   private static final int Id_setUTCMonth = 42;
   private static final int Id_setFullYear = 43;
   private static final int Id_setUTCFullYear = 44;
   private static final int Id_setYear = 45;
   private static final int Id_toISOString = 46;
   private static final int Id_toJSON = 47;
   private static final int MAX_PROTOTYPE_ID = 47;
   private static final int Id_toGMTString = 8;
   private static TimeZone thisTimeZone;
   private static double LocalTZA;
   private static DateFormat timeZoneFormatter;
   private static DateFormat localeDateTimeFormatter;
   private static DateFormat localeDateFormatter;
   private static DateFormat localeTimeFormatter;
   private double date;

   static void init(Scriptable scope, boolean sealed) {
      NativeDate obj = new NativeDate();
      obj.date = ScriptRuntime.NaN;
      obj.exportAsJSClass(47, scope, sealed);
   }

   private NativeDate() {
      super();
      if (thisTimeZone == null) {
         thisTimeZone = TimeZone.getDefault();
         LocalTZA = (double)thisTimeZone.getRawOffset();
      }

   }

   public String getClassName() {
      return "Date";
   }

   public Object getDefaultValue(Class typeHint) {
      if (typeHint == null) {
         typeHint = ScriptRuntime.StringClass;
      }

      return super.getDefaultValue(typeHint);
   }

   double getJSTimeValue() {
      return this.date;
   }

   protected void fillConstructorProperties(IdFunctionObject ctor) {
      this.addIdFunctionProperty(ctor, DATE_TAG, -3, "now", 0);
      this.addIdFunctionProperty(ctor, DATE_TAG, -2, "parse", 1);
      this.addIdFunctionProperty(ctor, DATE_TAG, -1, "UTC", 1);
      super.fillConstructorProperties(ctor);
   }

   protected void initPrototypeId(int id) {
      int arity;
      String s;
      switch (id) {
         case 1:
            arity = 1;
            s = "constructor";
            break;
         case 2:
            arity = 0;
            s = "toString";
            break;
         case 3:
            arity = 0;
            s = "toTimeString";
            break;
         case 4:
            arity = 0;
            s = "toDateString";
            break;
         case 5:
            arity = 0;
            s = "toLocaleString";
            break;
         case 6:
            arity = 0;
            s = "toLocaleTimeString";
            break;
         case 7:
            arity = 0;
            s = "toLocaleDateString";
            break;
         case 8:
            arity = 0;
            s = "toUTCString";
            break;
         case 9:
            arity = 0;
            s = "toSource";
            break;
         case 10:
            arity = 0;
            s = "valueOf";
            break;
         case 11:
            arity = 0;
            s = "getTime";
            break;
         case 12:
            arity = 0;
            s = "getYear";
            break;
         case 13:
            arity = 0;
            s = "getFullYear";
            break;
         case 14:
            arity = 0;
            s = "getUTCFullYear";
            break;
         case 15:
            arity = 0;
            s = "getMonth";
            break;
         case 16:
            arity = 0;
            s = "getUTCMonth";
            break;
         case 17:
            arity = 0;
            s = "getDate";
            break;
         case 18:
            arity = 0;
            s = "getUTCDate";
            break;
         case 19:
            arity = 0;
            s = "getDay";
            break;
         case 20:
            arity = 0;
            s = "getUTCDay";
            break;
         case 21:
            arity = 0;
            s = "getHours";
            break;
         case 22:
            arity = 0;
            s = "getUTCHours";
            break;
         case 23:
            arity = 0;
            s = "getMinutes";
            break;
         case 24:
            arity = 0;
            s = "getUTCMinutes";
            break;
         case 25:
            arity = 0;
            s = "getSeconds";
            break;
         case 26:
            arity = 0;
            s = "getUTCSeconds";
            break;
         case 27:
            arity = 0;
            s = "getMilliseconds";
            break;
         case 28:
            arity = 0;
            s = "getUTCMilliseconds";
            break;
         case 29:
            arity = 0;
            s = "getTimezoneOffset";
            break;
         case 30:
            arity = 1;
            s = "setTime";
            break;
         case 31:
            arity = 1;
            s = "setMilliseconds";
            break;
         case 32:
            arity = 1;
            s = "setUTCMilliseconds";
            break;
         case 33:
            arity = 2;
            s = "setSeconds";
            break;
         case 34:
            arity = 2;
            s = "setUTCSeconds";
            break;
         case 35:
            arity = 3;
            s = "setMinutes";
            break;
         case 36:
            arity = 3;
            s = "setUTCMinutes";
            break;
         case 37:
            arity = 4;
            s = "setHours";
            break;
         case 38:
            arity = 4;
            s = "setUTCHours";
            break;
         case 39:
            arity = 1;
            s = "setDate";
            break;
         case 40:
            arity = 1;
            s = "setUTCDate";
            break;
         case 41:
            arity = 2;
            s = "setMonth";
            break;
         case 42:
            arity = 2;
            s = "setUTCMonth";
            break;
         case 43:
            arity = 3;
            s = "setFullYear";
            break;
         case 44:
            arity = 3;
            s = "setUTCFullYear";
            break;
         case 45:
            arity = 1;
            s = "setYear";
            break;
         case 46:
            arity = 0;
            s = "toISOString";
            break;
         case 47:
            arity = 1;
            s = "toJSON";
            break;
         default:
            throw new IllegalArgumentException(String.valueOf(id));
      }

      this.initPrototypeMethod(DATE_TAG, id, s, arity);
   }

   public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      if (!f.hasTag(DATE_TAG)) {
         return super.execIdCall(f, cx, scope, thisObj, args);
      } else {
         int id = f.methodId();
         switch (id) {
            case -3:
               return ScriptRuntime.wrapNumber(now());
            case -2:
               String dataStr = ScriptRuntime.toString(args, 0);
               return ScriptRuntime.wrapNumber(date_parseString(dataStr));
            case -1:
               return ScriptRuntime.wrapNumber(jsStaticFunction_UTC(args));
            case 1:
               if (thisObj != null) {
                  return date_format(now(), 2);
               }

               return jsConstructor(args);
            case 47:
               if (thisObj instanceof NativeDate) {
                  return ((NativeDate)thisObj).toISOString();
               } else {
                  String toISOString = "toISOString";
                  Scriptable o = ScriptRuntime.toObject((Context)cx, (Scriptable)scope, (Object)thisObj);
                  Object tv = ScriptRuntime.toPrimitive(o, ScriptRuntime.NumberClass);
                  if (tv instanceof Number) {
                     double d = ((Number)tv).doubleValue();
                     if (d != d || Double.isInfinite(d)) {
                        return null;
                     }
                  }

                  Object toISO = o.get("toISOString", o);
                  if (toISO == NOT_FOUND) {
                     throw ScriptRuntime.typeError2("msg.function.not.found.in", "toISOString", ScriptRuntime.toString(o));
                  } else if (!(toISO instanceof Callable)) {
                     throw ScriptRuntime.typeError3("msg.isnt.function.in", "toISOString", ScriptRuntime.toString(o), ScriptRuntime.toString(toISO));
                  } else {
                     Object result = ((Callable)toISO).call(cx, scope, o, ScriptRuntime.emptyArgs);
                     if (!ScriptRuntime.isPrimitive(result)) {
                        throw ScriptRuntime.typeError1("msg.toisostring.must.return.primitive", ScriptRuntime.toString(result));
                     }

                     return result;
                  }
               }
            default:
               if (!(thisObj instanceof NativeDate)) {
                  throw incompatibleCallError(f);
               } else {
                  NativeDate realThis = (NativeDate)thisObj;
                  double t = realThis.date;
                  switch (id) {
                     case 2:
                     case 3:
                     case 4:
                        if (t == t) {
                           return date_format(t, id);
                        }

                        return "Invalid Date";
                     case 5:
                     case 6:
                     case 7:
                        if (t == t) {
                           return toLocale_helper(t, id);
                        }

                        return "Invalid Date";
                     case 8:
                        if (t == t) {
                           return js_toUTCString(t);
                        }

                        return "Invalid Date";
                     case 9:
                        return "(new Date(" + ScriptRuntime.toString(t) + "))";
                     case 10:
                     case 11:
                        return ScriptRuntime.wrapNumber(t);
                     case 12:
                     case 13:
                     case 14:
                        if (t == t) {
                           if (id != 14) {
                              t = LocalTime(t);
                           }

                           t = (double)YearFromTime(t);
                           if (id == 12) {
                              if (cx.hasFeature(1)) {
                                 if ((double)1900.0F <= t && t < (double)2000.0F) {
                                    t -= (double)1900.0F;
                                 }
                              } else {
                                 t -= (double)1900.0F;
                              }
                           }
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 15:
                     case 16:
                        if (t == t) {
                           if (id == 15) {
                              t = LocalTime(t);
                           }

                           t = (double)MonthFromTime(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 17:
                     case 18:
                        if (t == t) {
                           if (id == 17) {
                              t = LocalTime(t);
                           }

                           t = (double)DateFromTime(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 19:
                     case 20:
                        if (t == t) {
                           if (id == 19) {
                              t = LocalTime(t);
                           }

                           t = (double)WeekDay(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 21:
                     case 22:
                        if (t == t) {
                           if (id == 21) {
                              t = LocalTime(t);
                           }

                           t = (double)HourFromTime(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 23:
                     case 24:
                        if (t == t) {
                           if (id == 23) {
                              t = LocalTime(t);
                           }

                           t = (double)MinFromTime(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 25:
                     case 26:
                        if (t == t) {
                           if (id == 25) {
                              t = LocalTime(t);
                           }

                           t = (double)SecFromTime(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 27:
                     case 28:
                        if (t == t) {
                           if (id == 27) {
                              t = LocalTime(t);
                           }

                           t = (double)msFromTime(t);
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 29:
                        if (t == t) {
                           t = (t - LocalTime(t)) / (double)60000.0F;
                        }

                        return ScriptRuntime.wrapNumber(t);
                     case 30:
                        t = TimeClip(ScriptRuntime.toNumber(args, 0));
                        realThis.date = t;
                        return ScriptRuntime.wrapNumber(t);
                     case 31:
                     case 32:
                     case 33:
                     case 34:
                     case 35:
                     case 36:
                     case 37:
                     case 38:
                        t = makeTime(t, args, id);
                        realThis.date = t;
                        return ScriptRuntime.wrapNumber(t);
                     case 39:
                     case 40:
                     case 41:
                     case 42:
                     case 43:
                     case 44:
                        t = makeDate(t, args, id);
                        realThis.date = t;
                        return ScriptRuntime.wrapNumber(t);
                     case 45:
                        double year = ScriptRuntime.toNumber(args, 0);
                        if (year == year && !Double.isInfinite(year)) {
                           if (t != t) {
                              t = (double)0.0F;
                           } else {
                              t = LocalTime(t);
                           }

                           if (year >= (double)0.0F && year <= (double)99.0F) {
                              year += (double)1900.0F;
                           }

                           double day = MakeDay(year, (double)MonthFromTime(t), (double)DateFromTime(t));
                           t = MakeDate(day, TimeWithinDay(t));
                           t = internalUTC(t);
                           t = TimeClip(t);
                        } else {
                           t = ScriptRuntime.NaN;
                        }

                        realThis.date = t;
                        return ScriptRuntime.wrapNumber(t);
                     case 46:
                        return realThis.toISOString();
                     default:
                        throw new IllegalArgumentException(String.valueOf(id));
                  }
               }
         }
      }
   }

   private String toISOString() {
      if (this.date == this.date) {
         synchronized(isoFormat) {
            return isoFormat.format(new Date((long)this.date));
         }
      } else {
         String msg = ScriptRuntime.getMessage0("msg.invalid.date");
         throw ScriptRuntime.constructError("RangeError", msg);
      }
   }

   private static double Day(double t) {
      return Math.floor(t / (double)8.64E7F);
   }

   private static double TimeWithinDay(double t) {
      double result = t % (double)8.64E7F;
      if (result < (double)0.0F) {
         result += (double)8.64E7F;
      }

      return result;
   }

   private static boolean IsLeapYear(int year) {
      return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
   }

   private static double DayFromYear(double y) {
      return (double)365.0F * (y - (double)1970.0F) + Math.floor((y - (double)1969.0F) / (double)4.0F) - Math.floor((y - (double)1901.0F) / (double)100.0F) + Math.floor((y - (double)1601.0F) / (double)400.0F);
   }

   private static double TimeFromYear(double y) {
      return DayFromYear(y) * (double)8.64E7F;
   }

   private static int YearFromTime(double t) {
      int lo = (int)Math.floor(t / (double)8.64E7F / (double)366.0F) + 1970;
      int hi = (int)Math.floor(t / (double)8.64E7F / (double)365.0F) + 1970;
      if (hi < lo) {
         int temp = lo;
         lo = hi;
         hi = temp;
      }

      while(hi > lo) {
         int mid = (hi + lo) / 2;
         if (TimeFromYear((double)mid) > t) {
            hi = mid - 1;
         } else {
            lo = mid + 1;
            if (TimeFromYear((double)lo) > t) {
               return mid;
            }
         }
      }

      return lo;
   }

   private static double DayFromMonth(int m, int year) {
      int day = m * 30;
      if (m >= 7) {
         day += m / 2 - 1;
      } else if (m >= 2) {
         day += (m - 1) / 2 - 1;
      } else {
         day += m;
      }

      if (m >= 2 && IsLeapYear(year)) {
         ++day;
      }

      return (double)day;
   }

   private static int MonthFromTime(double t) {
      int year = YearFromTime(t);
      int d = (int)(Day(t) - DayFromYear((double)year));
      d -= 59;
      if (d < 0) {
         return d < -28 ? 0 : 1;
      } else {
         if (IsLeapYear(year)) {
            if (d == 0) {
               return 1;
            }

            --d;
         }

         int estimate = d / 30;
         int mstart;
         switch (estimate) {
            case 0:
               return 2;
            case 1:
               mstart = 31;
               break;
            case 2:
               mstart = 61;
               break;
            case 3:
               mstart = 92;
               break;
            case 4:
               mstart = 122;
               break;
            case 5:
               mstart = 153;
               break;
            case 6:
               mstart = 184;
               break;
            case 7:
               mstart = 214;
               break;
            case 8:
               mstart = 245;
               break;
            case 9:
               mstart = 275;
               break;
            case 10:
               return 11;
            default:
               throw Kit.codeBug();
         }

         return d >= mstart ? estimate + 2 : estimate + 1;
      }
   }

   private static int DateFromTime(double t) {
      int year = YearFromTime(t);
      int d = (int)(Day(t) - DayFromYear((double)year));
      d -= 59;
      if (d < 0) {
         return d < -28 ? d + 31 + 28 + 1 : d + 28 + 1;
      } else {
         if (IsLeapYear(year)) {
            if (d == 0) {
               return 29;
            }

            --d;
         }

         int mdays;
         int mstart;
         switch (d / 30) {
            case 0:
               return d + 1;
            case 1:
               mdays = 31;
               mstart = 31;
               break;
            case 2:
               mdays = 30;
               mstart = 61;
               break;
            case 3:
               mdays = 31;
               mstart = 92;
               break;
            case 4:
               mdays = 30;
               mstart = 122;
               break;
            case 5:
               mdays = 31;
               mstart = 153;
               break;
            case 6:
               mdays = 31;
               mstart = 184;
               break;
            case 7:
               mdays = 30;
               mstart = 214;
               break;
            case 8:
               mdays = 31;
               mstart = 245;
               break;
            case 9:
               mdays = 30;
               mstart = 275;
               break;
            case 10:
               return d - 275 + 1;
            default:
               throw Kit.codeBug();
         }

         d -= mstart;
         if (d < 0) {
            d += mdays;
         }

         return d + 1;
      }
   }

   private static int WeekDay(double t) {
      double result = Day(t) + (double)4.0F;
      result %= (double)7.0F;
      if (result < (double)0.0F) {
         result += (double)7.0F;
      }

      return (int)result;
   }

   private static double now() {
      return (double)System.currentTimeMillis();
   }

   private static double DaylightSavingTA(double t) {
      if (t < (double)0.0F) {
         int year = EquivalentYear(YearFromTime(t));
         double day = MakeDay((double)year, (double)MonthFromTime(t), (double)DateFromTime(t));
         t = MakeDate(day, TimeWithinDay(t));
      }

      Date date = new Date((long)t);
      return thisTimeZone.inDaylightTime(date) ? (double)3600000.0F : (double)0.0F;
   }

   private static int EquivalentYear(int year) {
      int day = (int)DayFromYear((double)year) + 4;
      day %= 7;
      if (day < 0) {
         day += 7;
      }

      if (IsLeapYear(year)) {
         switch (day) {
            case 0:
               return 1984;
            case 1:
               return 1996;
            case 2:
               return 1980;
            case 3:
               return 1992;
            case 4:
               return 1976;
            case 5:
               return 1988;
            case 6:
               return 1972;
         }
      } else {
         switch (day) {
            case 0:
               return 1978;
            case 1:
               return 1973;
            case 2:
               return 1985;
            case 3:
               return 1986;
            case 4:
               return 1981;
            case 5:
               return 1971;
            case 6:
               return 1977;
         }
      }

      throw Kit.codeBug();
   }

   private static double LocalTime(double t) {
      return t + LocalTZA + DaylightSavingTA(t);
   }

   private static double internalUTC(double t) {
      return t - LocalTZA - DaylightSavingTA(t - LocalTZA);
   }

   private static int HourFromTime(double t) {
      double result = Math.floor(t / (double)3600000.0F) % (double)24.0F;
      if (result < (double)0.0F) {
         result += (double)24.0F;
      }

      return (int)result;
   }

   private static int MinFromTime(double t) {
      double result = Math.floor(t / (double)60000.0F) % (double)60.0F;
      if (result < (double)0.0F) {
         result += (double)60.0F;
      }

      return (int)result;
   }

   private static int SecFromTime(double t) {
      double result = Math.floor(t / (double)1000.0F) % (double)60.0F;
      if (result < (double)0.0F) {
         result += (double)60.0F;
      }

      return (int)result;
   }

   private static int msFromTime(double t) {
      double result = t % (double)1000.0F;
      if (result < (double)0.0F) {
         result += (double)1000.0F;
      }

      return (int)result;
   }

   private static double MakeTime(double hour, double min, double sec, double ms) {
      return ((hour * (double)60.0F + min) * (double)60.0F + sec) * (double)1000.0F + ms;
   }

   private static double MakeDay(double year, double month, double date) {
      year += Math.floor(month / (double)12.0F);
      month %= (double)12.0F;
      if (month < (double)0.0F) {
         month += (double)12.0F;
      }

      double yearday = Math.floor(TimeFromYear(year) / (double)8.64E7F);
      double monthday = DayFromMonth((int)month, (int)year);
      return yearday + monthday + date - (double)1.0F;
   }

   private static double MakeDate(double day, double time) {
      return day * (double)8.64E7F + time;
   }

   private static double TimeClip(double d) {
      if (d == d && d != Double.POSITIVE_INFINITY && d != Double.NEGATIVE_INFINITY && !(Math.abs(d) > 8.64E15)) {
         return d > (double)0.0F ? Math.floor(d + (double)0.0F) : Math.ceil(d + (double)0.0F);
      } else {
         return ScriptRuntime.NaN;
      }
   }

   private static double date_msecFromDate(double year, double mon, double mday, double hour, double min, double sec, double msec) {
      double day = MakeDay(year, mon, mday);
      double time = MakeTime(hour, min, sec, msec);
      double result = MakeDate(day, time);
      return result;
   }

   private static double date_msecFromArgs(Object[] args) {
      double[] array = new double[7];

      for(int loop = 0; loop < 7; ++loop) {
         if (loop < args.length) {
            double d = ScriptRuntime.toNumber(args[loop]);
            if (d != d || Double.isInfinite(d)) {
               return ScriptRuntime.NaN;
            }

            array[loop] = ScriptRuntime.toInteger(args[loop]);
         } else if (loop == 2) {
            array[loop] = (double)1.0F;
         } else {
            array[loop] = (double)0.0F;
         }
      }

      if (array[0] >= (double)0.0F && array[0] <= (double)99.0F) {
         array[0] += (double)1900.0F;
      }

      return date_msecFromDate(array[0], array[1], array[2], array[3], array[4], array[5], array[6]);
   }

   private static double jsStaticFunction_UTC(Object[] args) {
      return TimeClip(date_msecFromArgs(args));
   }

   private static double date_parseString(String s) {
      try {
         if (s.length() == 24) {
            Date d;
            synchronized(isoFormat) {
               d = isoFormat.parse(s);
            }

            return (double)d.getTime();
         }
      } catch (ParseException var25) {
      }

      int year = -1;
      int mon = -1;
      int mday = -1;
      int hour = -1;
      int min = -1;
      int sec = -1;
      char c = '\u0000';
      char si = '\u0000';
      int i = 0;
      int n = -1;
      double tzoffset = (double)-1.0F;
      char prevc = 0;
      int limit = 0;
      boolean seenplusminus = false;
      limit = s.length();

      label334:
      while(i < limit) {
         c = s.charAt(i);
         ++i;
         if (c > ' ' && c != ',' && c != '-') {
            if (c != '(') {
               if ('0' > c || c > '9') {
                  if (c != '/' && c != ':' && c != '+' && c != '-') {
                     int st;
                     for(st = i - 1; i < limit; ++i) {
                        c = s.charAt(i);
                        if (('A' > c || c > 'Z') && ('a' > c || c > 'z')) {
                           break;
                        }
                     }

                     int letterCount = i - st;
                     if (letterCount < 2) {
                        return ScriptRuntime.NaN;
                     }

                     String wtb = "am;pm;monday;tuesday;wednesday;thursday;friday;saturday;sunday;january;february;march;april;may;june;july;august;september;october;november;december;gmt;ut;utc;est;edt;cst;cdt;mst;mdt;pst;pdt;";
                     int index = 0;
                     int wtbOffset = 0;

                     while(true) {
                        int wtbNext = wtb.indexOf(59, wtbOffset);
                        if (wtbNext < 0) {
                           return ScriptRuntime.NaN;
                        }

                        if (wtb.regionMatches(true, wtbOffset, s, st, letterCount)) {
                           if (index < 2) {
                              if (hour > 12 || hour < 0) {
                                 return ScriptRuntime.NaN;
                              }

                              if (index == 0) {
                                 if (hour == 12) {
                                    hour = 0;
                                 }
                              } else if (hour != 12) {
                                 hour += 12;
                              }
                           } else {
                              index -= 2;
                              if (index >= 7) {
                                 index -= 7;
                                 if (index < 12) {
                                    if (mon >= 0) {
                                       return ScriptRuntime.NaN;
                                    }

                                    mon = index;
                                 } else {
                                    index -= 12;
                                    switch (index) {
                                       case 0:
                                          tzoffset = (double)0.0F;
                                          continue label334;
                                       case 1:
                                          tzoffset = (double)0.0F;
                                          continue label334;
                                       case 2:
                                          tzoffset = (double)0.0F;
                                          continue label334;
                                       case 3:
                                          tzoffset = (double)300.0F;
                                          continue label334;
                                       case 4:
                                          tzoffset = (double)240.0F;
                                          continue label334;
                                       case 5:
                                          tzoffset = (double)360.0F;
                                          continue label334;
                                       case 6:
                                          tzoffset = (double)300.0F;
                                          continue label334;
                                       case 7:
                                          tzoffset = (double)420.0F;
                                          continue label334;
                                       case 8:
                                          tzoffset = (double)360.0F;
                                          continue label334;
                                       case 9:
                                          tzoffset = (double)480.0F;
                                          continue label334;
                                       case 10:
                                          tzoffset = (double)420.0F;
                                          continue label334;
                                       default:
                                          Kit.codeBug();
                                    }
                                 }
                              }
                           }
                           break;
                        }

                        wtbOffset = wtbNext + 1;
                        ++index;
                     }
                  } else {
                     prevc = c;
                  }
               } else {
                  for(n = c - 48; i < limit && '0' <= (c = s.charAt(i)) && c <= '9'; ++i) {
                     n = n * 10 + c - 48;
                  }

                  if (prevc != '+' && prevc != '-') {
                     if (n < 70 && (prevc != '/' || mon < 0 || mday < 0 || year >= 0)) {
                        if (c == ':') {
                           if (hour < 0) {
                              hour = n;
                           } else {
                              if (min >= 0) {
                                 return ScriptRuntime.NaN;
                              }

                              min = n;
                           }
                        } else if (c == '/') {
                           if (mon < 0) {
                              mon = n - 1;
                           } else {
                              if (mday >= 0) {
                                 return ScriptRuntime.NaN;
                              }

                              mday = n;
                           }
                        } else {
                           if (i < limit && c != ',' && c > ' ' && c != '-') {
                              return ScriptRuntime.NaN;
                           }

                           if (seenplusminus && n < 60) {
                              if (tzoffset < (double)0.0F) {
                                 tzoffset -= (double)n;
                              } else {
                                 tzoffset += (double)n;
                              }
                           } else if (hour >= 0 && min < 0) {
                              min = n;
                           } else if (min >= 0 && sec < 0) {
                              sec = n;
                           } else {
                              if (mday >= 0) {
                                 return ScriptRuntime.NaN;
                              }

                              mday = n;
                           }
                        }
                     } else {
                        if (year >= 0) {
                           return ScriptRuntime.NaN;
                        }

                        if (c > ' ' && c != ',' && c != '/' && i < limit) {
                           return ScriptRuntime.NaN;
                        }

                        year = n < 100 ? n + 1900 : n;
                     }
                  } else {
                     seenplusminus = true;
                     if (n < 24) {
                        n *= 60;
                     } else {
                        n = n % 100 + n / 100 * 60;
                     }

                     if (prevc == '+') {
                        n = -n;
                     }

                     if (tzoffset != (double)0.0F && tzoffset != (double)-1.0F) {
                        return ScriptRuntime.NaN;
                     }

                     tzoffset = (double)n;
                  }

                  prevc = 0;
               }
            } else {
               int depth = 1;

               while(i < limit) {
                  c = s.charAt(i);
                  ++i;
                  if (c == '(') {
                     ++depth;
                  } else if (c == ')') {
                     --depth;
                     if (depth <= 0) {
                        break;
                     }
                  }
               }
            }
         } else if (i < limit) {
            si = s.charAt(i);
            if (c == '-' && '0' <= si && si <= '9') {
               prevc = c;
            }
         }
      }

      if (year >= 0 && mon >= 0 && mday >= 0) {
         if (sec < 0) {
            sec = 0;
         }

         if (min < 0) {
            min = 0;
         }

         if (hour < 0) {
            hour = 0;
         }

         double msec = date_msecFromDate((double)year, (double)mon, (double)mday, (double)hour, (double)min, (double)sec, (double)0.0F);
         if (tzoffset == (double)-1.0F) {
            return internalUTC(msec);
         } else {
            return msec + tzoffset * (double)60000.0F;
         }
      } else {
         return ScriptRuntime.NaN;
      }
   }

   private static String date_format(double t, int methodId) {
      StringBuffer result = new StringBuffer(60);
      double local = LocalTime(t);
      if (methodId != 3) {
         appendWeekDayName(result, WeekDay(local));
         result.append(' ');
         appendMonthName(result, MonthFromTime(local));
         result.append(' ');
         append0PaddedUint(result, DateFromTime(local), 2);
         result.append(' ');
         int year = YearFromTime(local);
         if (year < 0) {
            result.append('-');
            year = -year;
         }

         append0PaddedUint(result, year, 4);
         if (methodId != 4) {
            result.append(' ');
         }
      }

      if (methodId != 4) {
         append0PaddedUint(result, HourFromTime(local), 2);
         result.append(':');
         append0PaddedUint(result, MinFromTime(local), 2);
         result.append(':');
         append0PaddedUint(result, SecFromTime(local), 2);
         int minutes = (int)Math.floor((LocalTZA + DaylightSavingTA(t)) / (double)60000.0F);
         int offset = minutes / 60 * 100 + minutes % 60;
         if (offset > 0) {
            result.append(" GMT+");
         } else {
            result.append(" GMT-");
            offset = -offset;
         }

         append0PaddedUint(result, offset, 4);
         if (timeZoneFormatter == null) {
            timeZoneFormatter = new SimpleDateFormat("zzz");
         }

         if (t < (double)0.0F) {
            int equiv = EquivalentYear(YearFromTime(local));
            double day = MakeDay((double)equiv, (double)MonthFromTime(t), (double)DateFromTime(t));
            t = MakeDate(day, TimeWithinDay(t));
         }

         result.append(" (");
         Date date = new Date((long)t);
         synchronized(timeZoneFormatter) {
            result.append(timeZoneFormatter.format(date));
         }

         result.append(')');
      }

      return result.toString();
   }

   private static Object jsConstructor(Object[] args) {
      NativeDate obj = new NativeDate();
      if (args.length == 0) {
         obj.date = now();
         return obj;
      } else if (args.length == 1) {
         Object arg0 = args[0];
         if (arg0 instanceof Scriptable) {
            arg0 = ((Scriptable)arg0).getDefaultValue((Class)null);
         }

         double date;
         if (arg0 instanceof CharSequence) {
            date = date_parseString(arg0.toString());
         } else {
            date = ScriptRuntime.toNumber(arg0);
         }

         obj.date = TimeClip(date);
         return obj;
      } else {
         double time = date_msecFromArgs(args);
         if (!Double.isNaN(time) && !Double.isInfinite(time)) {
            time = TimeClip(internalUTC(time));
         }

         obj.date = time;
         return obj;
      }
   }

   private static String toLocale_helper(double t, int methodId) {
      DateFormat formatter;
      switch (methodId) {
         case 5:
            if (localeDateTimeFormatter == null) {
               localeDateTimeFormatter = DateFormat.getDateTimeInstance(1, 1);
            }

            formatter = localeDateTimeFormatter;
            break;
         case 6:
            if (localeTimeFormatter == null) {
               localeTimeFormatter = DateFormat.getTimeInstance(1);
            }

            formatter = localeTimeFormatter;
            break;
         case 7:
            if (localeDateFormatter == null) {
               localeDateFormatter = DateFormat.getDateInstance(1);
            }

            formatter = localeDateFormatter;
            break;
         default:
            throw new AssertionError();
      }

      synchronized(formatter) {
         return formatter.format(new Date((long)t));
      }
   }

   private static String js_toUTCString(double date) {
      StringBuffer result = new StringBuffer(60);
      appendWeekDayName(result, WeekDay(date));
      result.append(", ");
      append0PaddedUint(result, DateFromTime(date), 2);
      result.append(' ');
      appendMonthName(result, MonthFromTime(date));
      result.append(' ');
      int year = YearFromTime(date);
      if (year < 0) {
         result.append('-');
         year = -year;
      }

      append0PaddedUint(result, year, 4);
      result.append(' ');
      append0PaddedUint(result, HourFromTime(date), 2);
      result.append(':');
      append0PaddedUint(result, MinFromTime(date), 2);
      result.append(':');
      append0PaddedUint(result, SecFromTime(date), 2);
      result.append(" GMT");
      return result.toString();
   }

   private static void append0PaddedUint(StringBuffer sb, int i, int minWidth) {
      if (i < 0) {
         Kit.codeBug();
      }

      int scale = 1;
      --minWidth;
      if (i >= 10) {
         if (i < 1000000000) {
            while(true) {
               int newScale = scale * 10;
               if (i < newScale) {
                  break;
               }

               --minWidth;
               scale = newScale;
            }
         } else {
            minWidth -= 9;
            scale = 1000000000;
         }
      }

      while(minWidth > 0) {
         sb.append('0');
         --minWidth;
      }

      while(scale != 1) {
         sb.append((char)(48 + i / scale));
         i %= scale;
         scale /= 10;
      }

      sb.append((char)(48 + i));
   }

   private static void appendMonthName(StringBuffer sb, int index) {
      String months = "JanFebMarAprMayJunJulAugSepOctNovDec";
      index *= 3;

      for(int i = 0; i != 3; ++i) {
         sb.append(months.charAt(index + i));
      }

   }

   private static void appendWeekDayName(StringBuffer sb, int index) {
      String days = "SunMonTueWedThuFriSat";
      index *= 3;

      for(int i = 0; i != 3; ++i) {
         sb.append(days.charAt(index + i));
      }

   }

   private static double makeTime(double date, Object[] args, int methodId) {
      boolean local = true;
      int maxargs;
      switch (methodId) {
         case 32:
            local = false;
         case 31:
            maxargs = 1;
            break;
         case 34:
            local = false;
         case 33:
            maxargs = 2;
            break;
         case 36:
            local = false;
         case 35:
            maxargs = 3;
            break;
         case 38:
            local = false;
         case 37:
            maxargs = 4;
            break;
         default:
            Kit.codeBug();
            maxargs = 0;
      }

      double[] conv = new double[4];
      if (date != date) {
         return date;
      } else {
         if (args.length == 0) {
            args = ScriptRuntime.padArguments(args, 1);
         }

         int i = 0;

         while(true) {
            if (i < args.length && i < maxargs) {
               conv[i] = ScriptRuntime.toNumber(args[i]);
               if (conv[i] == conv[i] && !Double.isInfinite(conv[i])) {
                  conv[i] = ScriptRuntime.toInteger(conv[i]);
                  ++i;
                  continue;
               }

               return ScriptRuntime.NaN;
            }

            double lorutime;
            if (local) {
               lorutime = LocalTime(date);
            } else {
               lorutime = date;
            }

            i = 0;
            int stop = args.length;
            double hour;
            if (maxargs >= 4 && i < stop) {
               hour = conv[i++];
            } else {
               hour = (double)HourFromTime(lorutime);
            }

            double min;
            if (maxargs >= 3 && i < stop) {
               min = conv[i++];
            } else {
               min = (double)MinFromTime(lorutime);
            }

            double sec;
            if (maxargs >= 2 && i < stop) {
               sec = conv[i++];
            } else {
               sec = (double)SecFromTime(lorutime);
            }

            double msec;
            if (maxargs >= 1 && i < stop) {
               msec = conv[i++];
            } else {
               msec = (double)msFromTime(lorutime);
            }

            double time = MakeTime(hour, min, sec, msec);
            double result = MakeDate(Day(lorutime), time);
            if (local) {
               result = internalUTC(result);
            }

            date = TimeClip(result);
            return date;
         }
      }
   }

   private static double makeDate(double date, Object[] args, int methodId) {
      boolean local = true;
      int maxargs;
      switch (methodId) {
         case 40:
            local = false;
         case 39:
            maxargs = 1;
            break;
         case 42:
            local = false;
         case 41:
            maxargs = 2;
            break;
         case 44:
            local = false;
         case 43:
            maxargs = 3;
            break;
         default:
            Kit.codeBug();
            maxargs = 0;
      }

      double[] conv = new double[3];
      if (args.length == 0) {
         args = ScriptRuntime.padArguments(args, 1);
      }

      int i = 0;

      while(true) {
         if (i < args.length && i < maxargs) {
            conv[i] = ScriptRuntime.toNumber(args[i]);
            if (conv[i] == conv[i] && !Double.isInfinite(conv[i])) {
               conv[i] = ScriptRuntime.toInteger(conv[i]);
               ++i;
               continue;
            }

            return ScriptRuntime.NaN;
         }

         double lorutime;
         if (date != date) {
            if (args.length < 3) {
               return ScriptRuntime.NaN;
            }

            lorutime = (double)0.0F;
         } else if (local) {
            lorutime = LocalTime(date);
         } else {
            lorutime = date;
         }

         i = 0;
         int stop = args.length;
         double year;
         if (maxargs >= 3 && i < stop) {
            year = conv[i++];
         } else {
            year = (double)YearFromTime(lorutime);
         }

         double month;
         if (maxargs >= 2 && i < stop) {
            month = conv[i++];
         } else {
            month = (double)MonthFromTime(lorutime);
         }

         double day;
         if (maxargs >= 1 && i < stop) {
            day = conv[i++];
         } else {
            day = (double)DateFromTime(lorutime);
         }

         day = MakeDay(year, month, day);
         double result = MakeDate(day, TimeWithinDay(lorutime));
         if (local) {
            result = internalUTC(result);
         }

         date = TimeClip(result);
         return date;
      }
   }

   protected int findPrototypeId(String s) {
      int id;
      String X;
      id = 0;
      X = null;
      label171:
      switch (s.length()) {
         case 6:
            int c = s.charAt(0);
            if (c == 103) {
               X = "getDay";
               id = 19;
            } else if (c == 116) {
               X = "toJSON";
               id = 47;
            }
            break;
         case 7:
            switch (s.charAt(3)) {
               case 'D':
                  int var24 = s.charAt(0);
                  if (var24 == 'g') {
                     X = "getDate";
                     id = 17;
                  } else if (var24 == 's') {
                     X = "setDate";
                     id = 39;
                  }
                  break label171;
               case 'T':
                  int var23 = s.charAt(0);
                  if (var23 == 'g') {
                     X = "getTime";
                     id = 11;
                  } else if (var23 == 's') {
                     X = "setTime";
                     id = 30;
                  }
                  break label171;
               case 'Y':
                  int var22 = s.charAt(0);
                  if (var22 == 'g') {
                     X = "getYear";
                     id = 12;
                  } else if (var22 == 's') {
                     X = "setYear";
                     id = 45;
                  }
                  break label171;
               case 'u':
                  X = "valueOf";
                  id = 10;
               default:
                  break label171;
            }
         case 8:
            switch (s.charAt(3)) {
               case 'H':
                  int var21 = s.charAt(0);
                  if (var21 == 'g') {
                     X = "getHours";
                     id = 21;
                  } else if (var21 == 's') {
                     X = "setHours";
                     id = 37;
                  }
                  break label171;
               case 'M':
                  int var20 = s.charAt(0);
                  if (var20 == 'g') {
                     X = "getMonth";
                     id = 15;
                  } else if (var20 == 's') {
                     X = "setMonth";
                     id = 41;
                  }
                  break label171;
               case 'o':
                  X = "toSource";
                  id = 9;
                  break label171;
               case 't':
                  X = "toString";
                  id = 2;
               default:
                  break label171;
            }
         case 9:
            X = "getUTCDay";
            id = 20;
            break;
         case 10:
            int var16 = s.charAt(3);
            if (var16 == 'M') {
               var16 = s.charAt(0);
               if (var16 == 'g') {
                  X = "getMinutes";
                  id = 23;
               } else if (var16 == 's') {
                  X = "setMinutes";
                  id = 35;
               }
            } else if (var16 == 'S') {
               var16 = s.charAt(0);
               if (var16 == 'g') {
                  X = "getSeconds";
                  id = 25;
               } else if (var16 == 's') {
                  X = "setSeconds";
                  id = 33;
               }
            } else if (var16 == 'U') {
               var16 = s.charAt(0);
               if (var16 == 'g') {
                  X = "getUTCDate";
                  id = 18;
               } else if (var16 == 's') {
                  X = "setUTCDate";
                  id = 40;
               }
            }
            break;
         case 11:
            switch (s.charAt(3)) {
               case 'F':
                  int var15 = s.charAt(0);
                  if (var15 == 'g') {
                     X = "getFullYear";
                     id = 13;
                  } else if (var15 == 's') {
                     X = "setFullYear";
                     id = 43;
                  }
                  break label171;
               case 'M':
                  X = "toGMTString";
                  id = 8;
                  break label171;
               case 'S':
                  X = "toISOString";
                  id = 46;
                  break label171;
               case 'T':
                  X = "toUTCString";
                  id = 8;
                  break label171;
               case 'U':
                  int var12 = s.charAt(0);
                  if (var12 == 'g') {
                     var12 = s.charAt(9);
                     if (var12 == 'r') {
                        X = "getUTCHours";
                        id = 22;
                     } else if (var12 == 't') {
                        X = "getUTCMonth";
                        id = 16;
                     }
                  } else if (var12 == 's') {
                     var12 = s.charAt(9);
                     if (var12 == 'r') {
                        X = "setUTCHours";
                        id = 38;
                     } else if (var12 == 't') {
                        X = "setUTCMonth";
                        id = 42;
                     }
                  }
                  break label171;
               case 's':
                  X = "constructor";
                  id = 1;
               default:
                  break label171;
            }
         case 12:
            int var11 = s.charAt(2);
            if (var11 == 'D') {
               X = "toDateString";
               id = 4;
            } else if (var11 == 'T') {
               X = "toTimeString";
               id = 3;
            }
            break;
         case 13:
            int var8 = s.charAt(0);
            if (var8 == 'g') {
               var8 = s.charAt(6);
               if (var8 == 'M') {
                  X = "getUTCMinutes";
                  id = 24;
               } else if (var8 == 'S') {
                  X = "getUTCSeconds";
                  id = 26;
               }
            } else if (var8 == 's') {
               var8 = s.charAt(6);
               if (var8 == 'M') {
                  X = "setUTCMinutes";
                  id = 36;
               } else if (var8 == 'S') {
                  X = "setUTCSeconds";
                  id = 34;
               }
            }
            break;
         case 14:
            int var7 = s.charAt(0);
            if (var7 == 'g') {
               X = "getUTCFullYear";
               id = 14;
            } else if (var7 == 's') {
               X = "setUTCFullYear";
               id = 44;
            } else if (var7 == 't') {
               X = "toLocaleString";
               id = 5;
            }
            break;
         case 15:
            int var6 = s.charAt(0);
            if (var6 == 'g') {
               X = "getMilliseconds";
               id = 27;
            } else if (var6 == 's') {
               X = "setMilliseconds";
               id = 31;
            }
         case 16:
         default:
            break;
         case 17:
            X = "getTimezoneOffset";
            id = 29;
            break;
         case 18:
            int c = s.charAt(0);
            if (c == 'g') {
               X = "getUTCMilliseconds";
               id = 28;
            } else if (c == 's') {
               X = "setUTCMilliseconds";
               id = 32;
            } else if (c == 't') {
               c = s.charAt(8);
               if (c == 'D') {
                  X = "toLocaleDateString";
                  id = 7;
               } else if (c == 'T') {
                  X = "toLocaleTimeString";
                  id = 6;
               }
            }
      }

      if (X != null && X != s && !X.equals(s)) {
         id = 0;
      }

      return id;
   }

   static {
      isoFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
      isoFormat.setLenient(false);
   }
}
