package com.yu.comment.controller;


import com.yu.comment.domain.dto.CommentDTO;
import com.yu.comment.domain.vo.CommentsVO;
import com.yu.comment.domain.vo.CommentVO;
import com.yu.comment.service.ICommentService;
import com.yu.common.domain.AjaxResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final ICommentService commentService;




}
