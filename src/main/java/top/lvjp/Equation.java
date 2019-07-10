package top.lvjp;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 算式对象
 * 封装算术表达式和其结果
 *
 * @author lvjp
 * @date 2019/7/10
 */
public class Equation {

    /**
     * 算数验证码需要的运算符
     */
    private static final String OPERATOR_CHARS = "+-x";

    /**
     * 随机数生成对象
     */
    private static ThreadLocalRandom random;

    /**
     * 算术运算符优先级
     */
    private static final Map<Character, Integer> operatorMap = new HashMap<>(5);

    static {
        random = ThreadLocalRandom.current();

        operatorMap.put('+', 1);
        operatorMap.put('-', 1);
        operatorMap.put('x', 2);
    }

    /**
     * 算术表达式
     */
    private char[] expression;

    /**
     * 表达式的值
     */
    private int result;


    private Equation(char[] expression, int result) {
        random = ThreadLocalRandom.current();

        this.expression = expression;
        this.result = result;
    }

    /**
     * 获取算式验证码
     *
     * @param operatorCount 运算符数量
     * @return Equation 算式验证码对象
     */
    public static Equation getEquation(int operatorCount) {
        int[] nums = new int[operatorCount + 1];
        char[] operates = new char[operatorCount];
        char[] expression = new char[operatorCount * 2 + 1];

        for (int i = 0; i < operatorCount + 1; i++) {
            nums[i] = random.nextInt(10);
            expression[i * 2] = (char) (nums[i] + 48);
        }
        for (int i = 0; i < operatorCount; i++) {
            operates[i] = OPERATOR_CHARS.charAt(random.nextInt(OPERATOR_CHARS.length()));
            expression[i * 2 + 1] = operates[i];
        }
        return new Equation(expression, getResult(nums, operates));
    }

    /**
     * 计算算式结果
     *
     * @param nums      数字数组
     * @param operators 运算符数组
     * @return 计算结果
     */
    private static int getResult(int[] nums, char[] operators) {
        Stack<Integer> numStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        numStack.push(nums[0]);
        numStack.push(nums[1]);
        operatorStack.push(operators[0]);
        for (int i = 1; i < operators.length; i++) {
            if (operatorMap.get(operators[i]) <= operatorMap.get(operatorStack.peek())) {
                int num2 = numStack.pop();
                int num1 = numStack.pop();
                numStack.push(computerResult(num1, num2, operatorStack.pop()));
            }
            numStack.push(nums[i + 1]);
            operatorStack.push(operators[i]);
        }
        while (!operatorStack.empty()) {
            int num2 = numStack.pop();
            int num1 = numStack.pop();
            numStack.push(computerResult(num1, num2, operatorStack.pop()));
        }
        return numStack.pop();
    }

    /**
     * 计算表达式
     *
     * @param num1     第一个数字
     * @param num2     第二个数字
     * @param operator 运算符
     * @return 返回表达式的值
     */
    private static int computerResult(Integer num1, Integer num2, Character operator) {
        int result = 0;
        switch (operator) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '*':
                result = num1 * num2;
                break;
            default:
        }
        return result;
    }

    protected char[] getExpression() {
        return expression;
    }

    protected int getResult() {
        return result;
    }
}
