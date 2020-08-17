/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.ast.NodeStream;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccTokenDocument;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType.PrimitiveType;
import net.sourceforge.pmd.lang.java.internal.JavaAstProcessor;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JConstructorSymbol;
import net.sourceforge.pmd.lang.java.symbols.JElementSymbol;
import net.sourceforge.pmd.lang.java.symbols.JMethodSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeParameterSymbol;
import net.sourceforge.pmd.lang.java.symbols.JVariableSymbol;
import net.sourceforge.pmd.lang.java.symbols.table.JSymbolTable;
import net.sourceforge.pmd.lang.java.typeresolution.typedefinition.JavaTypeDefinition;
import net.sourceforge.pmd.lang.symboltable.Scope;

/**
 * Acts as a bridge between outer parts of PMD and the restricted access
 * internal API of this package.
 *
 * <p><b>None of this is published API, and compatibility can be broken anytime!</b>
 * Use this only at your own risk.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
@InternalApi
public final class InternalApiBridge {


    private InternalApiBridge() {

    }

    @Deprecated
    public static ASTVariableDeclaratorId newVarId(String image) {
        ASTVariableDeclaratorId varid = new ASTVariableDeclaratorId(JavaParserImplTreeConstants.JJTVARIABLEDECLARATORID);
        varid.setImage(image);
        return varid;
    }


    /**
     * Creates a fake method name declaration for built-in methods from Java
     * like the Enum Method "valueOf".
     *
     * @param methodName     the method name
     * @param parameterTypes the reference types of each parameter of the method
     *
     * @return a method name declaration
     */
    public static ASTMethodDeclaration createBuiltInMethodDeclaration(final String methodName, final String... parameterTypes) {
        ASTMethodDeclaration methodDeclaration = new ASTMethodDeclaration(0);
        // InternalApiBridge.setModifier(methodDeclaration, JModifier.PUBLIC);

        methodDeclaration.setImage(methodName);

        ASTFormalParameters formalParameters = new ASTFormalParameters(0);
        methodDeclaration.addChild(formalParameters, 0);

        /*
         * jjtAddChild resizes it's child node list according to known indexes.
         * Going backwards makes sure the first time it gets the right size avoiding copies.
         */
        for (int i = parameterTypes.length - 1; i >= 0; i--) {
            ASTFormalParameter formalParameter = new ASTFormalParameter(0);
            formalParameters.addChild(formalParameter, i);

            ASTVariableDeclaratorId variableDeclaratorId = new ASTVariableDeclaratorId(0);
            variableDeclaratorId.setImage("arg" + i);
            formalParameter.addChild(variableDeclaratorId, 1);

            PrimitiveType primitive = PrimitiveType.fromToken(parameterTypes[i]);
            // TODO : this could actually be a primitive array...
            AbstractJavaNode type = primitive != null
                           ? new ASTPrimitiveType(primitive)
                           : new ASTClassOrInterfaceType(parameterTypes[i]);

            formalParameter.addChild(type, 0);
        }

        return methodDeclaration;
    }

    public static JavaccTokenDocument javaTokenDoc(String fullText) {
        return new JavaTokenDocument(fullText);
    }

    public static void setSymbol(SymbolDeclaratorNode node, JElementSymbol symbol) {
        if (node instanceof ASTMethodDeclaration) {
            ((ASTMethodDeclaration) node).setSymbol((JMethodSymbol) symbol);
        } else if (node instanceof ASTConstructorDeclaration) {
            ((ASTConstructorDeclaration) node).setSymbol((JConstructorSymbol) symbol);
        } else if (node instanceof ASTAnyTypeDeclaration) {
            ((AbstractAnyTypeDeclaration) node).setSymbol((JClassSymbol) symbol);
        } else if (node instanceof ASTVariableDeclaratorId) {
            ((ASTVariableDeclaratorId) node).setSymbol((JVariableSymbol) symbol);
        } else if (node instanceof ASTTypeParameter) {
            ((ASTTypeParameter) node).setSymbol((JTypeParameterSymbol) symbol);
        } else if (node instanceof ASTRecordComponentList) {
            ((ASTRecordComponentList) node).setSymbol((JConstructorSymbol) symbol);
        } else {
            throw new AssertionError("Cannot set symbol " + symbol + " on node " + node);
        }
    }

    public static void disambig(JavaAstProcessor processor, NodeStream<? extends JavaNode> nodes, ASTAnyTypeDeclaration context, boolean outsideContext) {
        AstDisambiguationPass.disambig(processor, nodes, context, outsideContext);
    }

    public static void disambig(JavaAstProcessor processor, ASTCompilationUnit root) {
        AstDisambiguationPass.disambig(processor, root);
    }

    public static void setSymbolTable(JavaNode node, JSymbolTable table) {
        ((AbstractJavaNode) node).setSymbolTable(table);
    }

    public static void setScope(JavaNode node, Scope scope) {
        ((AbstractJavaNode) node).setScope(scope);
    }

    public static void setComment(JavaNode node, Comment comment) {
        ((AbstractJavaNode) node).comment(comment);
    }

    public static void setQname(ASTAnyTypeDeclaration declaration, String binaryName, @Nullable String canon) {
        ((AbstractAnyTypeDeclaration) declaration).setBinaryName(binaryName, canon);
    }

    public static void setTypeDefinition(TypeNode node, JavaTypeDefinition definition) {
        if (node instanceof AbstractJavaTypeNode) {
            ((AbstractJavaTypeNode) node).setTypeDefinition(definition);
        }
    }

}