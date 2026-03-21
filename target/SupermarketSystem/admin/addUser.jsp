<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>添加用户</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial; background: #f5f5f5; }
        .header { background: #2c3e50; color: white; padding: 15px 30px; }
        .container { max-width: 600px; margin: 30px auto; }
        .form-box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #333; }
        input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; margin-right: 10px; }
        .btn-submit { background: #27ae60; color: white; }
        .btn-cancel { background: #95a5a6; color: white; }
    </style>
</head>
<body>
    <div class="header">
        <h1>添加用户</h1>
    </div>
    <div class="container">
        <div class="form-box">
            <form action="../user" method="post">
                <input type="hidden" name="action" value="add">

                <div class="form-group">
                    <label>用户名 *</label>
                    <input type="text" name="username" required>
                </div>

                <div class="form-group">
                    <label>密码 *</label>
                    <input type="password" name="password" required>
                </div>

                <div class="form-group">
                    <label>真实姓名 *</label>
                    <input type="text" name="realName" required>
                </div>

                <div class="form-group">
                    <label>角色 *</label>
                    <select name="role" required>
                        <option value="user">普通用户</option>
                        <option value="admin">管理员</option>
                        <option value="courier">配送员</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>电话</label>
                    <input type="text" name="phone">
                </div>

                <div>
                    <button type="submit" class="btn btn-submit">提交</button>
                    <button type="button" class="btn btn-cancel" onclick="history.back()">取消</button>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
