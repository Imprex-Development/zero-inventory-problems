package dev.imprex.zip.nms.api;

import java.util.List;

public record ItemStackContainerResult(int containerSize, List<ItemStackWithSlot> items) {
}
