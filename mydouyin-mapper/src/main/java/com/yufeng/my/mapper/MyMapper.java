/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yufeng.my.mapper;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 继承自己的MyMapper
 * 注意：该接口不能被扫描到，否则会出错，因为它有些方法不适合所有的数据库。
 * --------------------------------------------------------------------
 * 问：这个tk.mybatis.mapper.common.Mapper的作用是什么呀？
 * 答：tk.mybatis.mapper.common.Mapper是一个通用的Mapper接口，提供了一些基本的CRUD操作方法比如：
 * 通过继承 Mapper<T> 接口，你的 Mapper 接口自动获得以下方法：
 * 查询：selectByPrimaryKey、select、selectAll、selectCount 等
 * 插入：insert、insertSelective
 * 更新：updateByPrimaryKey、updateByPrimaryKeySelective
 * 删除：delete、deleteByPrimaryKey 等
 */
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T> {

}
