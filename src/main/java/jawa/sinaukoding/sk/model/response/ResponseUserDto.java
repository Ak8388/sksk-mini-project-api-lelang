package jawa.sinaukoding.sk.model.response;

import java.util.List;

public record ResponseUserDto(Long totalData,Long totalPage,Long page,Long offset,List<UserDto> userDto) {
    
}
