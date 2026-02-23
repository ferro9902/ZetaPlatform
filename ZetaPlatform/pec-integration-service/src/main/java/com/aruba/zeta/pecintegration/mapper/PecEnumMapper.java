package com.aruba.zeta.pecintegration.mapper;

import com.aruba.zeta.pecintegration.enums.EMailboxStatus;
import com.aruba.zeta.pecintegration.enums.EMessageStatus;
import com.aruba.zeta.pec.grpc.MailboxStatus;
import com.aruba.zeta.pec.grpc.MessageStatus;

/**
 * Maps between JPA entity enums and their proto-generated counterparts.
 *
 * Proto enum names are prefixed (e.g. MAILBOX_STATUS_ACTIVE) while the JPA
 * enums use shorter names (e.g. ACTIVE) stored as-is in the database.
 * Using fully-qualified proto class names avoids the same-simple-name collision.
 */
public final class PecEnumMapper {

    private PecEnumMapper() {}

    //  MailboxStatus

    public static MailboxStatus toProto(EMailboxStatus status) {
        return switch (status) {
            case ACTIVE    -> MailboxStatus.MAILBOX_STATUS_ACTIVE;
            case INACTIVE  -> MailboxStatus.MAILBOX_STATUS_INACTIVE;
            case SUSPENDED -> MailboxStatus.MAILBOX_STATUS_SUSPENDED;
        };
    }

    public static EMailboxStatus fromProto(MailboxStatus proto) {
        return switch (proto) {
            case MAILBOX_STATUS_ACTIVE    -> EMailboxStatus.ACTIVE;
            case MAILBOX_STATUS_INACTIVE  -> EMailboxStatus.INACTIVE;
            case MAILBOX_STATUS_SUSPENDED -> EMailboxStatus.SUSPENDED;
            default -> throw new IllegalArgumentException("Unrecognized MailboxStatus: " + proto);
        };
    }

    //  MessageStatus

    public static MessageStatus toProto(EMessageStatus status) {
        return switch (status) {
            case ACCEPTED  -> MessageStatus.MESSAGE_STATUS_ACCEPTED;
            case DELIVERED -> MessageStatus.MESSAGE_STATUS_DELIVERED;
            case FAILED    -> MessageStatus.MESSAGE_STATUS_FAILED;
            case SENT      -> MessageStatus.MESSAGE_STATUS_SENT;
        };
    }

    public static EMessageStatus fromProto(MessageStatus proto) {
        return switch (proto) {
            case MESSAGE_STATUS_ACCEPTED  -> EMessageStatus.ACCEPTED;
            case MESSAGE_STATUS_DELIVERED -> EMessageStatus.DELIVERED;
            case MESSAGE_STATUS_FAILED    -> EMessageStatus.FAILED;
            case MESSAGE_STATUS_SENT      -> EMessageStatus.SENT;
            default -> throw new IllegalArgumentException("Unrecognized MessageStatus: " + proto);
        };
    }
}
