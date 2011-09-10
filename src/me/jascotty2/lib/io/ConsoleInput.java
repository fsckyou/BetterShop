/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: Wrapper Class for Scanner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.jascotty2.lib.io;

import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsoleInput {
    
    protected Scanner kbin;

    public ConsoleInput() {
        kbin = new Scanner(System.in);
    }

    // hasNextLine can block if waiting for input
    public void Clear() {
        while (kbin.hasNextLine()) {
            //System.out.println("clearing.. ");
            kbin.nextLine();
        }

    }

    public double GetDouble(String prompt) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (!CheckInput.IsDouble(input)) {
                System.out.println("Invalid Input");
            } else {
                return Double.valueOf(input);
            }
        }
    }

    public double GetDouble(String prompt, double min, double max) {
        if(max<min) max=Double.POSITIVE_INFINITY;
        String input;
        double ret;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (!CheckInput.IsDouble(input)) {
                System.out.println("Invalid Input");
            } else {
                ret = Double.valueOf(input);
                if (ret < min) {
                    System.out.println("Input must be >= " + min);
                } else if (ret > max) {
                    System.out.println("Input must be <= " + max);
                } else {
                    return ret;
                }
            }
        }
    }

    public double GetDouble(String prompt, double min) {
        String input;
        double ret;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (!CheckInput.IsDouble(input)) {
                System.out.println("Invalid Input");
            } else {
                ret = Double.valueOf(input);
                if (ret < min) {
                    System.out.println("Input must be >= " + min);
                } else {
                    return ret;
                }
            }
        }
    }

    public int GetInt(String prompt) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim();
            if (!CheckInput.IsInt(input)) {
                System.out.println("Invalid Input");
            } else {
                return Integer.parseInt(input);
            }
        }
    }

    public int GetInt(String prompt, int min, int max) {
        if(max<min) max=Integer.MAX_VALUE;
        String input;
        int ret;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim();
            if (!CheckInput.IsInt(input)) {
                System.out.println("Invalid Input");
            } else {
                ret = Integer.parseInt(input);
                if (ret < min) {
                    System.out.println("Input must be >= " + min);
                } else if (ret > max) {
                    System.out.println("Input must be <= " + max);
                } else {
                    return ret;
                }
            }
        }
    }

    public int GetInt(String prompt, int min) {
        String input;
        int ret;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim();
            if (!CheckInput.IsInt(input)) {
                System.out.println("Invalid Input");
            } else {
                ret = Integer.parseInt(input);
                if (ret < min) {
                    System.out.println("Input must be >= " + min);
                } else {
                    return ret;
                }
            }
        }
    }

    public long GetLong(String prompt) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim();
            if (!CheckInput.IsLong(input)) {
                System.out.println("Invalid Input");
            } else {
                return Long.getLong(input);
            }
        }
    }

    public long GetLong(String prompt, long min, long max) {
        if (max < min) {
            max = Integer.MAX_VALUE;
        }
        String input;
        long ret;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim();
            if (!CheckInput.IsLong(input)) {
                System.out.println("Invalid Input");
            } else {
                ret = Long.parseLong(input);
                if (ret < min) {
                    System.out.println("Input must be >= " + min);
                } else if (ret > max) {
                    System.out.println("Input must be <= " + max);
                } else {
                    return ret;
                }
            }
        }
    }

    public long GetLong(String prompt, long min) {
        String input;
        long ret;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim();
            if (!CheckInput.IsLong(input)) {
                System.out.println("Invalid Input");
            } else {
                ret = Long.parseLong(input);
                if (ret < min) {
                    System.out.println("Input must be >= " + min);
                } else {
                    return ret;
                }
            }
        }
    }

    public String GetString(String prompt) {
        System.out.print(prompt);
        return kbin.nextLine();
    }

    public String GetString(String prompt, int minlen, int maxlen) {
        if(maxlen<minlen) maxlen=Integer.MAX_VALUE;
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (input.length() < minlen) {
                System.out.println("Input must be >= " + minlen + " characters long");
            } else if (input.length() > maxlen) {
                System.out.println("Input must be <= " + maxlen + " characters long");
            } else {
                return input;
            }
        }
    }

    public String GetString(String prompt, int minlen) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (input.length() < minlen) {
                System.out.println("Input must be >= " + minlen + " characters long");
            } else {
                return input;
            }
        }
    }

    public String GetString(String prompt, String pattern) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (!Pattern.matches(pattern, input)) {
                System.out.println("Input is in incorrect format");
            } else {
                return input;
            }
        }
    }
    
    public char GetChar(String prompt) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (input.length()==0) {
                System.out.println("Input Cannot be Blank");
            } else {
                return input.charAt(0);
            }
        }
    }
    
    public char GetChar(String prompt, char defaultChar) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (input.length()==0) {
                return defaultChar;
            } else {
                return input.charAt(0);
            }
        }
    }

    public char GetChar(String prompt, char[] allowedChar) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (input.length()==0) {
                System.out.println("Input Cannot be Blank");
            } else {
                for(char c : allowedChar){
                    if(Character.toLowerCase(input.charAt(0))==Character.toLowerCase(c)) return input.charAt(0); // c
                }
                System.out.println("Incorrect Entry");
            }
        }
    }
    public char GetChar(String prompt, char defaultChar, char[] allowedChar) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine();
            if (input.length()==0) {
                return defaultChar;
            } else {
                for(char c : allowedChar){
                    if(Character.toLowerCase(input.charAt(0))==Character.toLowerCase(c)) return input.charAt(0); // c
                }
                System.out.println("Incorrect Entry");
            }
        }
    }

    public boolean GetBool(String prompt) {
        String input;
        //Clear();
        while (true) {
            System.out.print(prompt);
            input = kbin.nextLine().trim().toLowerCase();
            if(input.length()>0){
                if(input.charAt(0)=='y' || input.charAt(0)=='1') return true;
                if(input.charAt(0)=='n' || input.charAt(0)=='0') return false;
                // else
                System.out.println("Input a 'y' or 'n'");
            }
        }
    }

} // end class ConsoleInput

