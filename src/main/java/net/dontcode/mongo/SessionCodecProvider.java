package net.dontcode.mongo;

import net.dontcode.session.Session;
import net.dontcode.session.SessionActionType;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class SessionCodecProvider implements CodecProvider {
        @Override
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            if (clazz == Session.class) {
                return (Codec<T>) new SessionCodec();
            } else if (clazz == SessionActionType.class) {
                return (Codec<T>) new SessionActionTypeCodec();
            }
            return null;
        }

    }