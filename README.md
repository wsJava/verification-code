# verification-code 验证码生成工具

### 用法
- 使用```new VerificationCode()```生成默认验证码工具;
- 也可以使用```new VerificationCode(CodeTypeEnum type)```构造方法指定验证码类型;

- 使用 ```getVerificationImage()``` 获取 ```VerificationImage``` 对象, 调用其 ```getBufferImage()``` 方法可获取验证码图片, ```getRightCode()```获取验证码;
- 使用 ```setHttpVerificationCode(HttpServletRequest request, HttpServletResponse response)``` 方法直接在 request.session 中加入验证码, 并设置 response 返回验证码图片. 

相关属性如下

属性 | 含义 | 默认值
:---: | :---: | :---:
SESSION_CODE_KEY| session 中存放 code 的 key |CODE-KEY
codeType | 默认验证码类型 | CodeTypeEnum.CHAR
codeChars |默认字符验证码用到的字符 |  23456789ABCDEFGHJKLMNPQRSTUVWXYZ
OPERATE_CHARS | 算数验证码需要的运算符 | +-x
lineCount | 默认干扰线的数量 | 15
codeSize | 默认验证码的长度 | 4
operateCount | 默认算式运算符个数 | 2
width | 默认验证码图片的宽 | 80
height | 默认验证码图片的高 | 30
fontBasisSize | 默认基础字体大小 | 16

> 注: 除 SESSION_CODE_KEY 和 OPERATOR_CHARS 外, 其他属性均提供 set 方法自定值. 但需要自己注意字体大小和验证码长度和图片大小的关系