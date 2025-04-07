# Syntax Checker
Made by Aman Sahu
## Test Cases
### Fail
| Test Case      | Status |
|----------------|--------|
| fail_01        | Y      |
| fail_02        | N      |
| fail_03 (a, b) | N      |
| fail_04        | N      |
| fail_05 (a, b) | N      |
| fail_06        | N      |
| fail_07        | N      |
| fail_08 (a, b) | N      |
| fail_09        | N      |
| fail_10        | N      |
**NOTE**: Fix line numbers

### Success
| Test Case | Status |
|-----------|--------|
| succ_01   | Y      |
| succ_02   | Y      |
| succ_03   | Y      |
| succ_04   | Y      |
| succ_05   | Y      |
| succ_06   | Y      |
| succ_07   | Y      |
| succ_08   | Y      |
| succ_09   | Y      |
| succ_10   | Y      |
|           |        |

## Compile flex using JFlex binary
``` bash
  java -jar jflex-1.6.1.jar Lexer.flex
```