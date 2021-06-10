package com.games.spyfall.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Such name already exists")
public class NameAlreadyExistException extends RuntimeException{
}
