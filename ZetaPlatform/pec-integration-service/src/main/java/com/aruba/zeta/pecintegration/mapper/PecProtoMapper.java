package com.aruba.zeta.pecintegration.mapper;

import com.aruba.zeta.pec.grpc.AttachmentRef;
import com.aruba.zeta.pec.grpc.Mailbox;
import com.aruba.zeta.pec.grpc.MailboxStatus;
import com.aruba.zeta.pec.grpc.MessageStatus;
import com.aruba.zeta.pec.grpc.PecMessage;
import com.aruba.zeta.pec.grpc.SendMessageResponse;
import com.aruba.zeta.pecintegration.dto.ArubaMailboxDto;
import com.aruba.zeta.pecintegration.dto.ArubaMessageDto;
import com.aruba.zeta.pecintegration.dto.ArubaSendMessageResponse;
import com.aruba.zeta.pecintegration.enums.EMailboxStatus;
import com.aruba.zeta.pecintegration.enums.EMessageStatus;

/**
 * Maps Aruba REST API DTOs to proto-generated response messages.
 *
 * <p>String-based status values from the Aruba API are parsed to domain enums
 * first, then converted to proto equivalents via {@link PecEnumMapper}.
 */
public final class PecProtoMapper {

    private PecProtoMapper() {}

    public static Mailbox toProto(ArubaMailboxDto dto) {
        return Mailbox.newBuilder()
                .setId(nullSafe(dto.getId()))
                .setPecAddress(nullSafe(dto.getPecAddress()))
                .setStatus(toMailboxStatus(dto.getStatus()))
                .build();
    }

    public static PecMessage toProto(ArubaMessageDto dto) {
        PecMessage.Builder builder = PecMessage.newBuilder()
                .setId(nullSafe(dto.getId()))
                .setSenderAddress(nullSafe(dto.getFrom()))
                .setRecipientAddress(nullSafe(dto.getTo()))
                .setSubject(nullSafe(dto.getSubject()))
                .setStatus(toMessageStatus(dto.getStatus()));
        if (dto.getAttachments() != null) {
            dto.getAttachments().forEach(a -> builder.addAttachments(
                    AttachmentRef.newBuilder()
                            .setDocumentId(nullSafe(a.getId()))
                            .setFileName(nullSafe(a.getFileName()))
                            .build()));
        }
        return builder.build();
    }

    public static SendMessageResponse toProto(ArubaSendMessageResponse dto) {
        return SendMessageResponse.newBuilder()
                .setMessageId(nullSafe(dto.getMessageId()))
                .setCurrentStatus(toMessageStatus(dto.getStatus()))
                .build();
    }

    // --- Enum helpers ---

    private static MailboxStatus toMailboxStatus(String status) {
        if (status == null) return MailboxStatus.MAILBOX_STATUS_UNSPECIFIED;
        try {
            return PecEnumMapper.toProto(EMailboxStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return MailboxStatus.MAILBOX_STATUS_UNSPECIFIED;
        }
    }

    private static MessageStatus toMessageStatus(String status) {
        if (status == null) return MessageStatus.MESSAGE_STATUS_UNSPECIFIED;
        try {
            return PecEnumMapper.toProto(EMessageStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return MessageStatus.MESSAGE_STATUS_UNSPECIFIED;
        }
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
}
