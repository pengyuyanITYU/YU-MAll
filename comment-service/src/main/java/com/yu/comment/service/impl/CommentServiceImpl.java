package com.yu.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yu.api.client.ItemClient;
import com.yu.api.client.OrderClient;
import com.yu.api.client.UserClient;
import com.yu.api.vo.ItemDetailVO;
import com.yu.comment.constants.LikeType;
import com.yu.comment.domain.dto.CommentDTO;
import com.yu.comment.domain.po.Comment;
import com.yu.comment.domain.vo.CommentsVO;
import com.yu.comment.domain.vo.CommentVO;
import com.yu.comment.mapper.CommentMapper;
import com.yu.comment.service.ICommentLikeService;
import com.yu.comment.service.ICommentService;
import com.yu.common.domain.AjaxResult;
import com.yu.common.utils.BeanUtils;
import com.yu.common.utils.CollUtils;
import com.yu.common.utils.AdministratorContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {




}
