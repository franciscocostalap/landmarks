package pt.isel.cn.utils;

import java.util.UUID;

public class UUIDRandomNameGenerator implements RandomNameGenerator {

    @Override
    public String generateName() {
        UUID uuid = UUID.randomUUID();
        String randomName = uuid.toString();

        randomName = randomName.replace("-", "");

        return randomName;
    }
}